/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import java.util.logging.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Verifier;

/**
 *
 * @author alex
 */
public class UserVerifier implements Verifier {

   Context context;
   protected UserVerifier(Context context) {
      this.context = context;
   }

   public Context getContext() {
      return context;
   }
   public Logger getLogger() {
      return context.getLogger();
   }
   public int verify(Request request, Response response) {
      return Verifier.RESULT_INVALID;
   }
}
