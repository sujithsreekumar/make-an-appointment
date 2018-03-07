package org.echs.resources;

import org.echs.model.Leave;
import org.echs.service.LeaveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.CREATED;

@Path("/leaves")
@Produces(value = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ManageLeaves {

    private static final Logger logger = LoggerFactory.getLogger(ManageLeaves.class);
    LeaveService leaveService = new LeaveService();

    @GET
    public Response getLeaves() throws SQLException {
        List<String> doctorsOnLeave = leaveService.getTodaysLeaves();
        return Response.status(Response.Status.OK).entity(doctorsOnLeave).build();
    }

    @POST
    @Path("/make")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateLeave(Leave leave) {
       leaveService.updateLeave(leave);
       return Response.status(CREATED).entity(leave).build();
    }

}
