/*
 * LoginApplication.java
 *
 * Created on September 7, 2007, 10:05 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.exist.restlet.login;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.exist.restlet.XMLDBResource;
import org.exist.restlet.util.ClassResourceFinder;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.Template;



/**
 *
 * @author alex
 */
public class LoginApplication extends Application
{
   static String NONCES = LoginApplication.class.getName()+".nonces";

   static class NonceCache {
      Map<Integer,Date> nonces;
      long expiry;
      long lastFlush;
      NonceCache(long expiry) {
         this.expiry = expiry;
         this.nonces = new HashMap<Integer,Date>();
         this.lastFlush = 0;
      }
      int get() {
         int nonce = (int)(Math.random()*Integer.MAX_VALUE);
         synchronized (nonces) {
            nonces.put(nonce, new Date());
         }
         return nonce;
      }

      void flush() {
         if (lastFlush==0 || (lastFlush+expiry)<System.currentTimeMillis()) {
            synchronized (nonces) {
               Set<Integer> keySet = new HashSet<Integer>();
               keySet.addAll(nonces.keySet());
               for (int nonce : keySet) {
                  Date generatedAt = nonces.get(nonce);
                  if ((generatedAt.getTime()+expiry)<System.currentTimeMillis()) {
                     nonces.remove(nonce);
                  }
               }
               lastFlush = System.currentTimeMillis();
            }
         }
      }

      boolean consume(int nonce) {
         Date generatedAt = null;
         synchronized (nonces) {
            generatedAt = nonces.remove(nonce);
         }
         flush();
         //System.out.println(nonce+" -> generatedAt="+generatedAt+", "+generatedAt.getTime()+" + "+expiry+" > "+System.currentTimeMillis());
         if (generatedAt==null) {
            return false;
         }
         if ((generatedAt.getTime()+expiry)<System.currentTimeMillis()) {
            return false;
         }
         return true;
      }
   }

   /**
    * Creates a new instance of LoginApplication
    */
   public LoginApplication(Context context)
   {
      super(context);
      getTunnelService().setEnabled(false);
      String expiryS = context.getParameters().getFirstValue(XMLDBResource.COOKIE_EXPIRY_NAME);
      // default to 30 minutes;
      long expiry = expiryS==null ? 30*60*1000 : Integer.parseInt(expiryS)*1000;
      getContext().getAttributes().put(NONCES,new NonceCache(expiry));
   }
   
   public Restlet createRoot() {
      Router router = new Router(getContext());
      router.attach("/",LoginView.class);
      router.attach("/auth",LoginAction.class).getTemplate().setMatchingMode(Template.MODE_EQUALS);
      router.attach("/status/check",CheckAction.class).getTemplate().setMatchingMode(Template.MODE_EQUALS);
      router.attach("/status",StatusAction.class).getTemplate().setMatchingMode(Template.MODE_EQUALS);
      router.attach("/expire",LogoutAction.class).getTemplate().setMatchingMode(Template.MODE_EQUALS);
      router.attach("/logout",LogoutView.class).getTemplate().setMatchingMode(Template.MODE_EQUALS);
      router.attach("/js/",new ClassResourceFinder(getContext(),LoginApplication.class,"js")).getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
      return router;
   }
   
}
