package org.rhq.enterprise.server.rest;

import java.net.URI;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.rhq.core.domain.measurement.MeasurementDefinition;
import org.rhq.core.domain.measurement.MeasurementSchedule;
import org.rhq.core.domain.resource.Agent;
import org.rhq.core.domain.resource.InventoryStatus;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.enterprise.server.RHQConstants;
import org.rhq.enterprise.server.core.AgentManagerLocal;
import org.rhq.enterprise.server.measurement.MeasurementScheduleManagerLocal;
import org.rhq.enterprise.server.resource.ResourceAlreadyExistsException;
import org.rhq.enterprise.server.resource.ResourceManagerLocal;
import org.rhq.enterprise.server.resource.ResourceTypeManagerLocal;
import org.rhq.enterprise.server.rest.domain.ResourceWithType;
import org.rhq.enterprise.server.rest.domain.StringValue;

/**
 * // TODO: Document this
 * @author Heiko W. Rupp
 */
@javax.annotation.Resource(name = "RHQ_DS", mappedName = RHQConstants.DATASOURCE_JNDI_NAME)
@Interceptors(SetCallerInterceptor.class)
@Stateless
public class FooBarHandler extends AbstractRestBean implements FooBarLocal {

//    private final Log log = LogFactory.getLog(FooBarHandler.class);

    @PersistenceContext(unitName = RHQConstants.PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;


    @EJB
    ResourceTypeManagerLocal resourceTypeManager;

    @EJB
    ResourceManagerLocal resMgr;

    @EJB
    AgentManagerLocal agentMgr;

    @EJB
    MeasurementScheduleManagerLocal scheduleMgr;




    @Override
    public Response createPlatform(@PathParam("name") String name, StringValue typeValue, @Context UriInfo uriInfo) {
        String typeName = typeValue.getValue();

        ResourceType type = resourceTypeManager.getResourceTypeByNameAndPlugin(typeName,"Platforms");
        if (type==null) {
            throw new StuffNotFoundException("Platform with type [" + typeName + "]");
        }

        String resourceKey = "p:" + name;
        Resource r = resMgr.getResourceByParentAndKey(caller,null,resourceKey,"Platforms",typeName);
        if (r!=null) {
            // platform exists - return it
            ResourceWithType rwt = fillRWT(r,uriInfo);

            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
            uriBuilder.path("/resource/{id}");
            URI uri = uriBuilder.build(r.getId());


            javax.ws.rs.core.Response.ResponseBuilder builder = Response.ok(rwt);
            builder.location(uri);
            return builder.build();

        }

        // Create a dummy agent per platform - otherwise we can't delete the platform later
        Agent agent ;
        agent = new Agent("dummy-agent:name"+name,"-dummy-p:"+name,12345,"http://foo.com/p:name/"+name,"abc-"+name);
        agentMgr.createAgent(agent);



        Resource platform = new Resource(resourceKey,name,type);
        platform.setUuid(resourceKey);
        platform.setAgent(agent);
        platform.setInventoryStatus(InventoryStatus.COMMITTED);
        platform.setModifiedBy(caller.getName());
        platform.setDescription(type.getDescription() + ". Created via REST-api");
        platform.setItime(System.currentTimeMillis());

        try {
            resMgr.createResource(caller,platform,-1);

            createSchedules(platform);

            ResourceWithType rwt = fillRWT(platform,uriInfo);
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
            uriBuilder.path("/resource/{id}");
            URI uri = uriBuilder.build(platform.getId());

            javax.ws.rs.core.Response.ResponseBuilder builder = Response.created(uri);
            builder.entity(rwt);
            return builder.build();


        } catch (ResourceAlreadyExistsException e) {
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void createSchedules(Resource resource) {
        ResourceType rt = resource.getResourceType();
        Set<MeasurementDefinition> definitions = rt.getMetricDefinitions ();
        for (MeasurementDefinition definition : definitions) {
            MeasurementSchedule schedule = new MeasurementSchedule(definition,resource);
            schedule.setEnabled(definition.isDefaultOn());
            schedule.setInterval(definition.getDefaultInterval());
            entityManager.persist(schedule);
        }
    }

    @Override
    public Response createResource(@PathParam("name") String name, StringValue typeValue,
                                   @QueryParam("plugin") String plugin, int parentId, UriInfo uriInfo) {

        Resource parent = resMgr.getResourceById(caller,parentId);
        if (parent==null)
            throw new StuffNotFoundException("Parent with id [" + parentId + "]");

        String typeName = typeValue.getValue();
        ResourceType resType = resourceTypeManager.getResourceTypeByNameAndPlugin(typeName,plugin);
        if (resType==null)
            throw new StuffNotFoundException("ResourceType with name [" + typeName + "] and plugin [" + plugin + "]");

        String resourceKey = "res:" + name + ":" + parentId;


        Resource r = resMgr.getResourceByParentAndKey(caller,null,resourceKey,plugin,typeName);
        if (r!=null) {
            // platform exists - return it
            ResourceWithType rwt = fillRWT(r,uriInfo);

            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
            uriBuilder.path("/resource/{id}");
            URI uri = uriBuilder.build(r.getId());


            javax.ws.rs.core.Response.ResponseBuilder builder = Response.ok(rwt);
            builder.location(uri);
            return builder.build();

        }


        Resource res = new Resource(resourceKey,name,resType);
        res.setUuid(resourceKey);
        res.setAgent(parent.getAgent());
        res.setParentResource(parent);
        res.setInventoryStatus(InventoryStatus.COMMITTED);
        res.setDescription(resType.getDescription() + ". Created via REST-api");

        try {
            resMgr.createResource(caller,res,parent.getId());

            createSchedules(res);

            ResourceWithType rwt = fillRWT(res,uriInfo);

            javax.ws.rs.core.Response.ResponseBuilder builder = Response.ok(rwt);
            return builder.build();


        } catch (ResourceAlreadyExistsException e) {
            throw new IllegalArgumentException(e);
        }


    }
}
