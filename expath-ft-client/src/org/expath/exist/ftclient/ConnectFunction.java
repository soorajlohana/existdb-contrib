/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2011 The eXist Project
 *  http://exist-db.org
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
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.expath.exist.ftclient;

import java.net.URI;

import org.apache.log4j.Logger;
import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.IntegerValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.AnyURIValue;
import org.exist.xquery.XPathException;

/**
 * Implements a method for opening a remote connection.
 * 
 * @author Adam Retter <adam@existsolutions.com>
 * @author Claudius Teodorescu <claudius.teodorescu@gmail.com>
 */


public class ConnectFunction extends BasicFunction {
    
    private static final FunctionReturnSequenceType RETURN_TYPE = new FunctionReturnSequenceType(Type.LONG, Cardinality.ZERO_OR_ONE, "an xs:long representing the connection handle." );
    private static final FunctionParameterSequenceType REMOTE_HOST_URI = new FunctionParameterSequenceType("remote-host-uri", Type.ANY_URI, Cardinality.EXACTLY_ONE, "The URI of the host to connect to." );
    private static final Logger log = Logger.getLogger(ConnectFunction.class);

    public final static FunctionSignature[] signatures = {
        new FunctionSignature(
            new QName("connect", ExistExpathFTClientModule.NAMESPACE_URI, ExistExpathFTClientModule.PREFIX),
            "This function is used to open a remote connection.",
            new SequenceType[] {
                REMOTE_HOST_URI
            },
            RETURN_TYPE
        ),
        new FunctionSignature(
            new QName("connect", ExistExpathFTClientModule.NAMESPACE_URI, ExistExpathFTClientModule.PREFIX),
            "This function is used to open a remote connection.",
            new SequenceType[] {
                REMOTE_HOST_URI,
                new FunctionParameterSequenceType("private-key", Type.STRING, Cardinality.EXACTLY_ONE, "The private key used for authentication." )
            },
            RETURN_TYPE
        )
    };

    /**
     * ConnectFunction Constructor.
     *
     * @param  context    The Context of the calling XQuery
     * @param  signature  DOCUMENT ME!
     */
    public ConnectFunction(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    /**
     * evaluate the call to the xquery connect() function, it is really the main entry point of this class.
     *
     * @param   args             arguments from the get-connection() function call
     * @param   contextSequence  the Context Sequence to operate on (not used here internally!)
     *
     * @return  A xs:long representing a handle to the connection
     *
     * @throws  XPathException  DOCUMENT ME!
     *
     * @see     org.exist.xquery.BasicFunction#eval(org.exist.xquery.value.Sequence[], org.exist.xquery.value.Sequence)
     */
    @Override
    public Sequence eval( Sequence[] args, Sequence contextSequence ) throws XPathException {
        
        Sequence result = Sequence.EMPTY_SEQUENCE;
        Object remoteConnection = null;
        String clientPrivateKey = "";
        if (args.length == 2)
        {
        	clientPrivateKey = args[1].getStringValue();
        }
        URI remoteHostUri = ((AnyURIValue)args[0].itemAt(0)).toURI();

        //get the connection object
        try {
            remoteConnection = org.expath.ftclient.Connect.connect(remoteHostUri, clientPrivateKey);
        } catch (Exception ex) {
        	throw new XPathException(ex.getMessage());
        }

        //store the connection and return the uid handle of the connection
        result = new IntegerValue(ExistExpathFTClientModule.storeRemoteConnection(context, remoteConnection));
        log.info("Claudius - connection id: " + result);
        
        return result;
    }
}