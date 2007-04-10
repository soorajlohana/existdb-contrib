/*
 *  eXist Mail Module Extension SendEmailFunction
 *  Copyright (C) 2006 Adam Retter <adam.retter@devon.gov.uk>
 *  www.adamretter.co.uk
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *  $Id: SendEmailFunction.java,v 1.12 2006/03/01 13:52:00 deliriumsky Exp $
 */

package nl.ow.dilemma.exist.module.mail;

import java.io.StringWriter;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.w3c.dom.Node;

/**
 * eXist Mail Module Extension SendEmailFunction
 *
 * The email sending functionality of the eXist Mail Module Extension that
 * allows email to be sent from XQuery using either SMTP or Sendmail.
 *
 * @author Adam Retter <adam.retter@devon.gov.uk>
 * @serial 2006-03-01
 * @version 1.11
 *
 * @see org.exist.xquery.BasicFunction#BasicFunction(org.exist.xquery.XQueryContext, org.exist.xquery.FunctionSignature)
 */
public class SendEmailFunction extends BasicFunction {
    
    private String charset;
    
    public final static FunctionSignature signature =
            new FunctionSignature(
            new QName("send-email", MailModule.NAMESPACE_URI, MailModule.PREFIX),
            "Sends an email $a through the SMTP Server $b, or if $b is () tries to use the local sendmail program. $a is the email in the following format <mail><from/><to/><cc/><bcc/><subject/><message><text/><xhtml/></message></mail>. $c defines the charset value used in the \"Content-Type\" message header (Defaults to UTF-8)",
            new SequenceType[]
    {
        new SequenceType(Type.NODE, Cardinality.EXACTLY_ONE),
        new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE),
        new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE)
    },
            new SequenceType(Type.BOOLEAN, Cardinality.EXACTLY_ONE));
    
    /**
     * SendEmail Constructor
     *
     * @param context	The Context of the calling XQuery
     */
    public SendEmailFunction(XQueryContext context) {
        super(context, signature);
    }
    
    /**
     * evaluate the call to the xquery send-email function,
     * it is really the main entry point of this class
     *
     * @param args		arguments from the send-email() function call
     * @param contextSequence	the Context Sequence to operate on (not used here internally!)
     * @return		A sequence representing the result of the send-email() function call
     *
     * @see org.exist.xquery.BasicFunction#eval(org.exist.xquery.value.Sequence[], org.exist.xquery.value.Sequence)
     */
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
        try {
            //get the charset parameter, default to UTF-8
            if (!args[2].isEmpty()) {
                charset =  args[2].getStringValue();
            } else {
                charset =  "UTF-8";
            }
            
            String smtpHost=null;
            //Send email with Sendmail or SMTP?
            if(!args[1].isEmpty()) {
                smtpHost=args[1].getStringValue();
            } else {
                smtpHost="localhost";
            }
            
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.host", smtpHost);
            
            Session mailSession = Session.getDefaultInstance(props, null);
            Transport transport = mailSession.getTransport();
            
            MimeMessage message = new MimeMessage(mailSession);
            
            ParseMailXML( ((NodeValue)args[0].itemAt(0)).getNode(), message );
             
            transport.connect();
            transport.sendMessage(message,
                    message.getRecipients(Message.RecipientType.TO));
            transport.close();
            
            return(BooleanValue.TRUE);
        } catch(Exception e) {
            e.printStackTrace();
            LOG.error(e);
            throw new XPathException("OOPS: " + e.getMessage(), e);
        }
        
    }
    
    
    /**
     * Constructs a mail Object from an XML representation of an email
     *
     * The XML email Representation is expected to look something like this
     *
     * <mail>
     * 	<from></from>
     * 	<to></to>
     * 	<cc></cc>
     * 	<bcc></bcc>
     * 	<subject></subject>
     * 	<mailNode>
     * 		<text></text>
     * 		<xhtml></xhtml>
     * 	</mailNode>
     * </mail>
     *
     *
     * @param mailNode	The XML mail Node
     * @return A mail Object representing the XML mail Node
     */
    private void ParseMailXML(Node mailNode, Message message) throws Exception {
        
        
        //Make sure that mailNode has a Mail node
        if(mailNode.getNodeType() == Node.ELEMENT_NODE && mailNode.getLocalName().equals("mail")) {
            //Get the First Child
            Node child = mailNode.getFirstChild();
            while(child != null) {
                //Parse each of the child nodes
                if(child.getNodeType() == Node.ELEMENT_NODE && child.hasChildNodes()) {
                    
                    if(child.getLocalName().equals("from")) {
                        String value=child.getFirstChild().getNodeValue();
                        message.setFrom( new InternetAddress(value) );
                        LOG.debug("from="+value);
                        
                    } else if(child.getLocalName().equals("to")) {
                        String value=child.getFirstChild().getNodeValue();
                        message.addRecipient(Message.RecipientType.TO,
                                new InternetAddress(value));
                        LOG.debug("to="+value);
                        
                    } else if(child.getLocalName().equals("cc")) {
                        String value=child.getFirstChild().getNodeValue();
                        message.addRecipient(Message.RecipientType.CC,
                                new InternetAddress(value));
                        LOG.debug("cc="+value);
                        
                    } else if(child.getLocalName().equals("bcc")) {
                        String value=child.getFirstChild().getNodeValue();
                        message.addRecipient(Message.RecipientType.CC,
                                new InternetAddress(value));
                        LOG.debug("bcc="+value);
                        
                    } else if(child.getLocalName().equals("subject")) {
                        String value=child.getFirstChild().getNodeValue();
                        message.setSubject(value);
                        LOG.debug("subject="+value);
                        
                    } else if(child.getLocalName().equals("message")) {
                        //If the mailNode node, then parse the child text and xhtml nodes
                        Node bodyPart = child.getFirstChild();
                        while(bodyPart != null) {
                            if(bodyPart.getLocalName().equals("text")) {
                                String value=child.getFirstChild().getNodeValue();
                                message.setText( value );
                            } else if(bodyPart.getLocalName().equals("xhtml")) {
                                //Convert everything inside <xhtml></xhtml> to text
                                TransformerFactory transFactory = TransformerFactory.newInstance();
                                Transformer transformer = transFactory.newTransformer();
                                DOMSource source = new DOMSource(bodyPart.getFirstChild());
                                StringWriter strWriter = new StringWriter();
                                StreamResult result = new StreamResult(strWriter);
                                transformer.transform(source, result);
                                
                                message.setText(strWriter.toString());
                            }
                            
                            //next body part
                            bodyPart = bodyPart.getNextSibling();
                        }
                        
                    }
                }
                
                //next node
                child = child.getNextSibling();
                
            }
        }
    }
    
}
