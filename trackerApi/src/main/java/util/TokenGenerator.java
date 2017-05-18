/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.UUID;

/**
 *
 * @author alachman
 */
public class TokenGenerator {

    public static String generateTokenForUser(long id) {
        StringBuilder builder = new StringBuilder(UUID.randomUUID().toString().replaceAll("-", ""));
        builder.append(id);
        builder.append(System.currentTimeMillis());
        return builder.toString();

    }

}
