/**
 * This file is part of the CRISTAL-iSE REST API.
 * Copyright (c) 2001-2016 The CRISTAL Consortium. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * http://www.fsf.org/licensing/licenses/lgpl.html
 */
package org.cristalise.restapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.cristalise.kernel.common.AccessRightsException;
import org.cristalise.kernel.common.InvalidDataException;
import org.cristalise.kernel.common.ObjectNotFoundException;
import org.cristalise.kernel.lookup.AgentPath;
import org.cristalise.kernel.process.Gateway;
import org.cristalise.kernel.utils.Logger;

@Path("login")
public class CookieLogin extends RestHandler {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(@QueryParam("user") String user, @QueryParam("pass") String pass, @Context UriInfo uri) {
    	String authToken = null;
        try {
            authToken = Gateway.getAuthenticator().authenticate(user, pass, null);
        }
        catch (InvalidDataException e) {
            Logger.error(e);
            throw new WebApplicationException("Problem logging in");
        }
        catch (AccessRightsException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.UNAUTHORIZED);
        }

        AgentPath agentPath;
        try {
            agentPath = Gateway.getLookup().getAgentPath(user);
        }
        catch (ObjectNotFoundException e) {
            Logger.error(e);
            throw ItemUtils.createWebAppException("Agent '" + user + "' not found", Response.Status.UNAUTHORIZED);
        }

        // create and set cookie
        AuthData agentData = new AuthData(agentPath);
        try {
            NewCookie cookie;

            int cookieLife = Gateway.getProperties().getInt("REST.loginCookieLife", 0);
            if (cookieLife > 0)
                cookie = new NewCookie(COOKIENAME, authToken, "/", null, null, cookieLife, false);
            else
                cookie = new NewCookie(COOKIENAME, authToken);
            return Response.ok().cookie(cookie).build();
        }
        catch (Exception e) {
            Logger.error(e);
            throw ItemUtils.createWebAppException("Error creating cookie");
        }
    }
}
