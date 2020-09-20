package com.asurax.utils;

import java.util.Base64;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Arrays;
import java.util.Set;
import java.util.jar.Manifest;
import java.io.InputStream;
import java.util.Enumeration;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.jar.JarEntry;
import java.util.Vector;
import java.util.jar.JarFile;
import com.github.manolo8.darkbot.utils.AuthAPI;

public class VerifierChecker
{
    private static final String META_INF = "META-INF/";
    private static final String SIG_PREFIX = "META-INF/SIG-";
    private static final byte[] POPCORN_PUB;

    public static void checkAuthenticity() {
        final AuthAPI api = getAuthApi();
        if (!api.isAuthenticated()) {
            api.setupAuth();
        }
    }

    public static AuthAPI getAuthApi() {
        final AuthAPI instance = AuthAPI.getInstance();
        try (final JarFile jf = new JarFile(findPathJar(instance.getClass()), true)) {
            final Vector<JarEntry> entriesVec = new Vector<JarEntry>();
            final byte[] buffer = new byte[8192];
            final Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                final JarEntry je = entries.nextElement();
                entriesVec.addElement(je);
                try (final InputStream is = jf.getInputStream(je)) {
                    while (is.read(buffer, 0, buffer.length) != -1) {}
                }
            }
            final Manifest man = jf.getManifest();
            if (man == null) {
                throw new SecurityException("Verifier not signed");
            }
            final Enumeration<JarEntry> e = entriesVec.elements();
            final Set<Certificate> allowedCerts = new HashSet<Certificate>();
            while (e.hasMoreElements()) {
                final JarEntry je2 = e.nextElement();
                final String name = je2.getName();
                if (!je2.isDirectory()) {
                    if (signatureRelated(name)) {
                        continue;
                    }
                    final Boolean signed = checkCertificates(je2.getCertificates(), allowedCerts);
                    if (signed == null || !signed) {
                        throw new SecurityException("Verifier not properly signed");
                    }
                    continue;
                }
            }
        }
        catch (Exception e2) {
            throw new SecurityException("Failed to check verifier signature", e2);
        }
        return instance;
    }

    private static Boolean checkCertificates(final Certificate[] certs, final Set<Certificate> allowedCerts) {
        if (certs == null || certs.length == 0) {
            return null;
        }
        for (final Certificate cert : certs) {
            if (allowedCerts.contains(cert)) {
                return true;
            }
            if (Arrays.equals(VerifierChecker.POPCORN_PUB, cert.getPublicKey().getEncoded())) {
                allowedCerts.add(cert);
                return true;
            }
        }
        return false;
    }

    private static boolean signatureRelated(final String name) {
        final String ucName = name.toUpperCase(Locale.ENGLISH);
        return ucName.equals("META-INF/MANIFEST.MF") || ucName.equals("META-INF/") || (ucName.startsWith("META-INF/SIG-") && ucName.indexOf("/") == ucName.lastIndexOf("/")) || (ucName.startsWith("META-INF/") && (ucName.endsWith(".SF") || ucName.endsWith(".DSA") || ucName.endsWith(".RSA") || ucName.endsWith(".EC")) && ucName.indexOf("/") == ucName.lastIndexOf("/"));
    }

    private static String findPathJar(final Class<?> context) throws IllegalStateException {
        final String rawName = context.getName();
        final int idx = rawName.lastIndexOf(46);
        final String classFileName = ((idx == -1) ? rawName : rawName.substring(idx + 1)) + ".class";
        final String uri = context.getResource(classFileName).toString();
        if (uri.startsWith("file:")) {
            throw new IllegalStateException("This class has been loaded from a directory and not from a jar file.");
        }
        if (!uri.startsWith("jar:file:")) {
            final int idx2 = uri.indexOf(58);
            final String protocol = (idx2 == -1) ? "(unknown)" : uri.substring(0, idx2);
            throw new IllegalStateException("This class has been loaded remotely via the " + protocol + " protocol. Only loading from a jar on the local file system is supported.");
        }
        final int idx2 = uri.indexOf(33);
        if (idx2 == -1) {
            throw new IllegalStateException("You appear to have loaded this class from a local jar file, but I can't make sense of the URL!");
        }
        try {
            final String fileName = URLDecoder.decode(uri.substring("jar:file:".length(), idx2), Charset.defaultCharset().name());
            return new File(fileName).getAbsolutePath();
        }
        catch (UnsupportedEncodingException e) {
            throw new InternalError("default charset doesn't exist. Your VM is borked.");
        }
    }

    static {
        POPCORN_PUB = Base64.getDecoder().decode("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAzqOpdk4bdoMlk3IkDaHFSOpwyYmpfACHCuhNDiml13Wf9J4D9g4kszOV3Qz+FT1jdYO36pWCxI01Mr03dPLky9COwD//dQM/KRFBe7Z0wRsC91n5fprgWIkwdKs79en6vmynyyPi5hAgwpifKm4o9DP5xR0YP/KRoPH8ZekS+STBxPsLdy/BeBiFFFgNQ0usRNIkLBKYWFJ3A3br4QkVicOLvycHKrfsN9K2Ly25VXyYo/GJdeEY30ixKhsCdo9xc50ERVuEVkzqlqLUSFDgHyFAO1o91QIhG+G0GURlI8iSt/b5cn39DM0OtkL+1TqqwT4NJqBH8nHSok8lReu1o/iMu9VbrFyJTUK0qUjVhnySJQV3i5oV0oxwqPodDihvmNUhMUel5gM/yRnloKKEYk+74MLdClgcFWmbEYFUQF32vxdkKpGYYRmzH0Y8+pGKE8nBbe1/eKg2HVu42vStb/yKp7DpxQ05UovJ5nrXA7lUfwCwBOwzOmCjn3AKNhH+Hbg/tutwZn5KNU4zJCRUEM4FLkCCJMEDJTGnpjxNO/vUMEm+Co6RgrD1vBIgRzNxaYh1BInbDdlKncXhysHNR5b6Et2POyCrlrM4flvFvTg42/zbI1ElKgEFNbhujdP5fBtxeD1hkc5UUa8JtYHsHa0LBrTUfnr3F29rRwHFpFUCAwEAAQ==");
    }
}
