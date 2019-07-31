package org.echs.resources;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.echs.model.Doctors;
import org.echs.service.LeaveService;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("resources")
public class MyResource {
    LeaveService leaveService = new LeaveService();

    @GET
    @Path("/doctors")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getDepartmentsAndDoctors() throws Exception {
        final List<Doctors> doctors = leaveService.getDepartmentsAndDoctors();
        return Response.status(Response.Status.OK).entity(doctors).build();
    }

}



