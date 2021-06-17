package com.example.jorge.androidapp.framework.security;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.example.jorge.androidapp.R;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

public class ApplicationCipher {

    private Context mContext = null;
    private String TAG = "APP_SHARE_FILE";
    private boolean doLog = false;
    private InputStream cert_user_privado = null;
    private InputStream cert_firmado_publico = null;
    private InputStream cert_firma_privado = null;
    private InputStream cert_user_publica = null;

    public ApplicationCipher(Context context, boolean log) {
        this.doLog = log;
        this.mContext = context;
    }


    public byte[] cipherData(String mensaje) throws Exception {


        /* *******************************************************************************
         * *******************************************************************************
         * *************************PASO 1 : Se cifra con AES el mensaje******************
         * *******************************************************************************
         * *******************************************************************************
         */
        byte[] buffer_mensaje = mensaje.getBytes();
        ByteArrayOutputStream baos = null;
        try {
            // generate a key
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            // To use 256 bit keys, you need the "unlimited strength" encryption policy files from Sun.
            keygen.init(128);
            byte[] key_session_AES = keygen.generateKey().getEncoded();
            SecretKeySpec skeySpec = new SecretKeySpec(key_session_AES, "AES");
            // build the initialization vector.  This example is all zeros, but it
            // could be any value or generated using a random number generator.
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            // initialize the cipher for encrypt mode
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);


            // Ciframos el mensaje
            byte[] mensaje_cifrado = cipher.doFinal(buffer_mensaje);
            logI("Cifrado el mensaje con clave AES : " + mensaje_cifrado);

            /* *******************************************************************************
             * *******************************************************************************
             * ******* Paso 2: cifro la clave AES con la clave Publica del certificado *******
             * *******************************************************************************
             * *******************************************************************************
             */


            if (cert_user_privado == null)
                cert_user_privado = mContext.getResources().openRawResource(R.raw.cert_user_priv);

            CertificateFactory cf_cert_firmado = CertificateFactory.getInstance("X509");
            X509Certificate certFirmado = (X509Certificate) cf_cert_firmado.generateCertificate(cert_user_privado);
            //OBTENEMOS LA CLAVE PUBLICA DE LA CA
            PublicKey publiKeycert = certFirmado.getPublicKey();
            // CLAVE PUBLICA EXTRAIDA DEL CERTIFICADO FIRMADO


            // PASO 2: Crear cifrador RSA
            Cipher cifrador = Cipher.getInstance("RSA"); // Hace uso del provider BC

            // PASO 2a: Poner cifrador en modo CIFRADO

            cifrador.init(Cipher.ENCRYPT_MODE, publiKeycert);  // Cifra con la clave publica
            /*Aqui se cifra la clave AES*/
            byte[] bufferCifrado_sk = cifrador.doFinal(key_session_AES);

            /* *******************************************************************************
             * *******************************************************************************
             * Paso 3: Heacemos un hash del mensaje cifrado con la clave privada del certificadoo
             * *******************************************************************************
             * *******************************************************************************
             */

            KeyStore ks, ks2;
            String PassStore = "ppppp";

            char[] fraseclave = PassStore.toCharArray();

            ks = KeyStore.getInstance("PKCS12");
            if (cert_firmado_publico == null)
                cert_firmado_publico = mContext.getResources().openRawResource(R.raw.cert_firma_publica);
            //InputStream cert_firmado_publico = mContext.getResources().openRawResource(R.raw.cert_firma_publica);
            ks.load(cert_firmado_publico, fraseclave);
            Enumeration<String> enumeration = ks.aliases();
            String alias = null;
            int n = 1;
            while (enumeration.hasMoreElements()) {
                alias = enumeration.nextElement();
                X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);
                n = n + 1;

            }
            Key key = ks.getKey(alias, fraseclave);
            PrivateKey privKey = (PrivateKey) key;
            Signature sig = Signature.getInstance("SHA512withRSA");
            sig.initSign(privKey);
            sig.update(buffer_mensaje);

            baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // escribimos el primer elemento: mensaje cifrado
            oos.writeObject(mensaje_cifrado);
            // escribimos el segundo elemento: clave AES cifrada clave publica de B
            oos.writeObject(bufferCifrado_sk);
            // escribimos el tercer elemento: Hash del mensaje cifrado clave privada de A
            oos.writeObject(sig.sign());
            // escribimos el cuerto elemento: certificado de A
            //oos.writeObject(buffer_fichero_cert);
            if (cert_firma_privado == null)
                cert_firma_privado = mContext.getResources().openRawResource(R.raw.cert_firma_priv);
            byte[] bytes = IOUtils.toByteArray(cert_firma_privado);
            oos.writeObject(bytes);
            oos.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
        return baos.toByteArray();
    }


    /**
     * @param res
     * @return
     */
    public String decipherData(byte[] res) {
        byte[] mensaje_descifrado = {};
        try {

            byte[] mensaje_cifrado = null;
            byte[] clave_sesion_cifrada = null;

            byte[] signature = null;
            byte[] certificate = null;

            ByteArrayInputStream fis = new ByteArrayInputStream(res);
            ObjectInputStream ois = new ObjectInputStream(fis);

            Object o2 = ois.readObject();
            mensaje_cifrado = (byte[]) o2;

            //Obtenemos primer elemento. Mensaje encriptado

            o2 = ois.readObject();
            clave_sesion_cifrada = (byte[]) o2;

            //Obtenemos segundo elemento. Clave aescriptada
            o2 = ois.readObject();
            signature = (byte[]) o2;

            //Obtenemos tercer elemento. HASH mensaje cifrado clave privada A
            o2 = ois.readObject();
            certificate = (byte[]) o2;

            //Obtenemos cuarto elemento. Certificado de A
            KeyStore ks, ks2;
            String PassStore = "ppppp";

            char[] fraseclave = PassStore.toCharArray();

            ks = KeyStore.getInstance("PKCS12");

            //InputStream fichp12 = mContext.getResources().openRawResource(R.raw.cert_user_publica);
            if (cert_user_publica == null)
                cert_user_publica = mContext.getResources().openRawResource(R.raw.cert_user_publica);

            ks.load(cert_user_publica, fraseclave);
            Enumeration<String> enumeration = ks.aliases();

            String alias = null;
            int n = 1;
            while (enumeration.hasMoreElements()) {
                alias = (String) enumeration.nextElement();
                X509Certificate certificate2 = (X509Certificate) ks.getCertificate(alias);
                n = n + 1;

            }

            Key key = ks.getKey(alias, fraseclave);

            PrivateKey privKey = (PrivateKey) key;

            PrivateKey claveprivada_leida = (PrivateKey) key;

            // PASO 2: Crear cifrador RSA
            Cipher cifrador = Cipher.getInstance("RSA"); // Hace uso del provider BC

            cifrador.init(Cipher.DECRYPT_MODE, claveprivada_leida); // Descrifra con la clave privada

            //Obtenemos la clave AES descifrando con la clave privada del usuario
            byte[] bufferPlano2 = cifrador.doFinal(clave_sesion_cifrada);

            // build the initialization vector.  This example is all zeros, but it
            // could be any value or generated using a random number generator.
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            // initialize the cipher for encrypt mode
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            SecretKeySpec skeySpec = new SecretKeySpec(bufferPlano2, "AES");

            // reinitialize the cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

            // desciframos el array de bytes
            mensaje_descifrado = cipher.doFinal(mensaje_cifrado);
            logI("Mensaje descrifrado " + new String(mensaje_descifrado));

            // Verificamos la firmn enviada por A

            InputStream input = new ByteArrayInputStream(certificate);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(input);

            PublicKey pk = cert.getPublicKey();
            ois.close();
            Signature s = Signature.getInstance("SHA512withRSA");
            s.initVerify(pk);
            s.update(mensaje_descifrado);


            if (s.verify(signature)) {
            } else {
                // No verificada
            }

        } catch (
                Exception e) {


            Log.v("Excepcio", "EXce", e);

        }

        return new String(mensaje_descifrado);

    }

    @CallSuper
    public void logI(String msg) {
        if (doLog) {
            Timber.tag(TAG);
            Timber.i(getTag() + msg);
        }
    }

    @CallSuper
    public void logI(String msg, Object... args) {
        if (doLog) {
            Timber.tag(TAG);
            Timber.i(getTag() + msg, args);
        }
    }


    private String getTag() {
        return "CIFRADOR __";
    }

    public void closeAll() {
        try {
            if (cert_user_privado != null)
                cert_user_privado.close();
            if (cert_firma_privado != null)
                cert_firma_privado.close();
            if (cert_firmado_publico != null)
                cert_firmado_publico.close();
            if (cert_user_publica != null)
                cert_user_publica.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
