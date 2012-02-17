package org.rhq.enterprise.server.rest;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.Response;

import org.rhq.enterprise.server.rest.domain.StringValue;

/**
 * // TODO: Document this
 * @author Heiko W. Rupp
 */
@Local
@Path("/foo")
@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
public interface FooBarLocal {

    @POST
    @Path("cp/{name}")
    public javax.ws.rs.core.Response createPlatform(@PathParam("name") String name, StringValue type, @Context UriInfo uriInfo);

    @POST
    @Path("cr/{name}")
    public javax.ws.rs.core.Response createResource(@PathParam("name") String name, StringValue type,
                                                    @QueryParam("plugin") String plugin,
                                                    @QueryParam("parentId") int parentId, @Context UriInfo uriInfo);
}
