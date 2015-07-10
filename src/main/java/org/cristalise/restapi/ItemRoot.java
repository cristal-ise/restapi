package org.cristalise.restapi;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.cristalise.kernel.common.ObjectNotFoundException;
import org.cristalise.kernel.entity.agent.Job;
import org.cristalise.kernel.entity.proxy.AgentProxy;
import org.cristalise.kernel.entity.proxy.ItemProxy;
import org.cristalise.kernel.lookup.AgentPath;
import org.cristalise.kernel.persistency.ClusterStorage;
import org.cristalise.kernel.process.Gateway;
import org.cristalise.kernel.utils.Logger;

@Path("item")
public class ItemRoot extends ItemUtils {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{uuid}")
	public Response getItemSummary(@PathParam("uuid") String uuid,
			@Context UriInfo uri) {
		ItemProxy item = getProxy(uuid);
		
		LinkedHashMap<String, Object> itemSummary = new LinkedHashMap<String, Object>();
		itemSummary.put("Name", item.getName());
		try {
			itemSummary.put("Properties", getPropertySummary(item));
		} catch (ObjectNotFoundException e) {
			Logger.error(e);
			throw new WebApplicationException("No Properties found", 400);
		}
		
		itemSummary.put("Data", enumerate(item, ClusterStorage.VIEWPOINT, "data", uri));
		
		return toJSON(itemSummary);
	}
	
	@OPTIONS
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{uuid}")
	public Response getJobs(@PathParam("uuid") String uuid, 
			@QueryParam("agent") String agentName, @Context UriInfo uri) {
		ItemProxy item = getProxy(uuid);
		AgentPath agentPath;
		try {
			agentPath = Gateway.getLookup().getAgentPath(agentName);
		} catch (ObjectNotFoundException e) {
			Logger.error(e);
			throw new WebApplicationException("Agent '"+agentName+"' not found", 404);
		}
		AgentProxy agentProxy;
		try {
			agentProxy = (AgentProxy)Gateway.getProxyManager().getProxy(agentPath);
		} catch (ObjectNotFoundException e) {
			Logger.error(e);
			throw new WebApplicationException("Agent proxy for '"+agentName+"' not found");
		}
		
		List<Job> jobList;
		try {
			jobList = item.getJobList(agentProxy);
		} catch (Exception e) {
			Logger.error(e);
			throw new WebApplicationException("Error loading joblist");
		}
		
		ArrayList<Object> jobListData = new ArrayList<Object>();
		for (Job job : jobList) {
			jobListData.add(jsonJob(item, job, uri));
		}
		
		return toJSON(jobListData);
		
	}

}