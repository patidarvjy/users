#!/usr/bin/env bash
#By default a RSA256 Key is generated!
keytool -genkeypair -alias jwt -keyalg RSA -dname "CN=jwt, L=Vienna, S=Vienna, C=AT" -keypass mySecretKey -keystore devjwt.jks -storepass mySecretKey
keytool -list -rfc --keystore devjwt.jks | openssl x509 -inform pem -pubkey >> public.cert