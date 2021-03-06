/*
 * Copyright (c) 2018-2019, FusionAuth, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package io.fusionauth.jwt.ec;

import io.fusionauth.jwt.InvalidJWTSignatureException;
import io.fusionauth.jwt.InvalidKeyTypeException;
import io.fusionauth.jwt.JWTVerifierException;
import io.fusionauth.jwt.MissingPublicKeyException;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.pem.domain.PEM;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.util.Objects;

/**
 * @author Daniel DeGroff
 */
public class ECVerifier implements Verifier {
  private final ECPublicKey publicKey;

  private ECVerifier(String publicKey) {
    PEM pem = PEM.decode(publicKey);
    if (pem.publicKey == null) {
      throw new MissingPublicKeyException("The provided PEM encoded string did not contain a public key.");
    }

    if (!(pem.publicKey instanceof ECPublicKey)) {
      throw new InvalidKeyTypeException("Expecting an EC public key, but found " + pem.publicKey.getAlgorithm() + " / " + pem.publicKey.getFormat() + "");
    }

    this.publicKey = pem.getPublicKey();
  }

  /**
   * Return a new instance of the EC Verifier with the provided public key.
   *
   * @param publicKey The EC public key PEM.
   * @return a new instance of the EC verifier.
   */
  public static ECVerifier newVerifier(String publicKey) {
    Objects.requireNonNull(publicKey);
    return new ECVerifier(publicKey);
  }

  /**
   * Return a new instance of the EC Verifier with the provided public key.
   *
   * @param path The path to the EC public key PEM.
   * @return a new instance of the EC verifier.
   */
  public static ECVerifier newVerifier(Path path) {
    Objects.requireNonNull(path);

    try {
      return new ECVerifier(new String(Files.readAllBytes(path)));
    } catch (IOException e) {
      throw new JWTVerifierException("Unable to read the file from path [" + path.toAbsolutePath().toString() + "]", e);
    }
  }

  /**
   * Return a new instance of the EC Verifier with the provided public key.
   *
   * @param bytes The bytes of the EC public key PEM.
   * @return a new instance of the EC verifier.
   */
  public static ECVerifier newVerifier(byte[] bytes) {
    Objects.requireNonNull(bytes);
    return new ECVerifier(new String(bytes));
  }

  @Override
  @SuppressWarnings("Duplicates")
  public boolean canVerify(Algorithm algorithm) {
    switch (algorithm) {
      case ES256:
      case ES384:
      case ES512:
        return true;
      default:
        return false;
    }
  }

  @Override
  public void verify(Algorithm algorithm, byte[] message, byte[] signature) {
    Objects.requireNonNull(algorithm);
    Objects.requireNonNull(message);
    Objects.requireNonNull(signature);

    try {
      Signature verifier = Signature.getInstance(algorithm.getName());
      verifier.initVerify(publicKey);
      verifier.update(message);

      byte[] derEncoded = new ECDSASignature(signature).derEncode();
      if (!(verifier.verify(derEncoded))) {
        throw new InvalidJWTSignatureException();
      }
    } catch (InvalidKeyException | IOException | NoSuchAlgorithmException | SignatureException | SecurityException e) {
      throw new JWTVerifierException("An unexpected exception occurred when attempting to verify the JWT", e);
    }
  }
}
