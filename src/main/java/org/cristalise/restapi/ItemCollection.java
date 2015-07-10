package org.cristalise.restapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.cristalise.kernel.collection.Collection;
import org.cristalise.kernel.common.ObjectNotFoundException;
import org.cristalise.kernel.entity.proxy.ItemProxy;
import org.cristalise.kernel.persistency.ClusterStorage;

@Path("/item/{uuid}/collection")
public class ItemCollection extends ItemUtils {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollections(@PathParam("uuid") String uuid, @Context UriInfo uri) {
		ItemProxy item = getProxy(uuid);
		return toJSON(enumerate(item, ClusterStorage.COLLECTION, "collection", uri));
	}
	
	@GET
	@Path("{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLastCollection(@PathParam("uuid") String uuid, @PathParam("name") String collName, 
			@Context UriInfo uri) {
		ItemProxy item = getProxy(uuid);
		try {
			return toJSON(item.getCollection(collName));
		} catch (ObjectNotFoundException e) {
			throw new WebApplicationException(404);
		}
	}
	
	@GET
	@Path("{name}/version")
	public Response getCollectionVersions(@PathParam("uuid") String uuid, @PathParam("name") String collName, 
			@Context UriInfo uri) {
		ItemProxy item = getProxy(uuid);
		return toJSON(enumerate(item, ClusterStorage.COLLECTION+"/"+collName, "collection/"+collName+"/version", uri));
	}
	
	@GET
	@Path("{name}/version/{version}")
	public Response getCollectionVersion(@PathParam("uuid") String uuid, @PathParam("name") String collName, 
			@PathParam("version") String collVersion, @Context UriInfo uri) {
		ItemProxy item = getProxy(uuid);
		try {
			return toJSON(item.getCollection(collName, collVersion.equals("last")?null:Integer.valueOf(collVersion)));
		} catch (ObjectNotFoundException e) {
			throw new WebApplicationException(404);
		} catch (NumberFormatException e) {
			throw new WebApplicationException(404);
		}
	}
}