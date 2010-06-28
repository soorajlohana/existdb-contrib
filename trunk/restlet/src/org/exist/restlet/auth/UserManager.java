/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import org.exist.security.User;

/**
 *
 * @author alex
 */
public interface UserManager {
   User getUser(String name);
}
