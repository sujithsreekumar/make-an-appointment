package org.echs.resources;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.echs.model.Doctors;
import org.echs.service.LeaveService;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("resources")
public class MyResource {
    LeaveService leaveService = new LeaveService();

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }

    @GET
    @Path("/doctors")
    public Response getDepartmentsAndDoctors() throws Exception {
        final List<Doctors> doctors = leaveService.getDepartmentsAndDoctors();
        return Response.status(Response.Status.OK).entity(doctors).build();
    }

}



