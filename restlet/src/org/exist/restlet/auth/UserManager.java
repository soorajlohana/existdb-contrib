/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import org.exist.security.User;
import org.restlet.security.Verifier;

/**
 *
 * @author alex
 */
public interface UserManager extends Verifier {
   User getUser(String name);
}
