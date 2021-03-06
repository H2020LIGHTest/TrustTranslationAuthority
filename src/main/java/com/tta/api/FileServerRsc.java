/**
 *
 *LIGHTest Trust Translation Authority
 *Copyright © 2018 Atos Spain SA
 *
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation, either version 3 of the License, or
 *(at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.tta.api;

import java.io.File;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.tta.commons.conf.Configuration;
import com.tta.commons.cte.PropertyNames;
import com.tta.commons.model.TTModelST;

@Path("/TrustTranslationDeclaration")
public class FileServerRsc {
	
	/**
	 * method to retrieve a Trust Translation file
	 * @param headers HTTP headers of the request
	 * @param receivedName name of the file to get
	 * @return the requested file. if "accept" header contains xml string the file will be provided in xml format, other wise it will be tpl format.
	 */
	@GET
	@Path("/{key}")
	public Response getFile(@Context HttpHeaders headers, @PathParam("key") String receivedName) {
		
		if (!TTAManagerRsc.initialized_) {
			TTAManagerRsc.initialize();
		}
		
		String signS = Configuration.getConfiguration().getProperty(PropertyNames.SIGN_TRANSLATION_FILES);
		boolean sign = Boolean.parseBoolean(signS);
		
		
		String[] fa = receivedName.split("\\.");
		String finalName="";
		
		//in this case the name recevied also contains the extension so the file extension is into the file name
		if ((fa[fa.length-1].equalsIgnoreCase("xml")) || (fa[fa.length-1].equalsIgnoreCase("tpl"))) {
			finalName = receivedName;
		}else {
			List<String> l = headers.getRequestHeader("Accept");
			
			for (String s : l) {
				if (s.contains("xml")) {
					finalName = receivedName + ".xml";
				}else {
					if (sign)
						finalName = receivedName + ".tpl.p7s";
					else
						finalName = receivedName + ".tpl";
				}
			}
		}
		String filePath = TTModelST.getModel().getTTDFCont().getFile(finalName);
		if (filePath != null) {
			File f = new File (filePath);
		
			ResponseBuilder responseBuilder = Response.ok((Object) f);
			responseBuilder.header("Content-Disposition", "attachment; filename=\""+finalName+"\"");
			return responseBuilder.build();
		}else {
			return Response.status(Response.Status.NOT_FOUND).entity("File not Found").build();
		}
	
	}
}
