/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.tribe.web.controller;

import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.tribe.Tribe;
import org.openmrs.module.tribe.TribeService;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class TribeListController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		log.info("initBinder");

		super.initBinder(request, binder);
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		log.info("onSubmit");

		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			
			String[] tribeList = request.getParameterValues("tribeId");
			TribeService ts = ((TribeService)Context.getService(TribeService.class));
			String action = ""; 
			if (request.getParameter("retire") != null)
				action = "retire";
			else if (request.getParameter("unretire") != null)
				action = "unretire";
			
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String changed = msa.getMessage("general.changed");
			String notChanged = msa.getMessage("general.cannot.change");
			
			if(tribeList != null) {
				for (String t : tribeList) {
					try {
						if (action.equals("retire"))
							ts.retireTribe(ts.getTribe(Integer.valueOf(t)));
						if (action.equals("unretire"))
							ts.unretireTribe(ts.getTribe(Integer.valueOf(t)));
						if (!success.equals("")) success += "<br/>";
						success += t + " " + changed;
					}
					catch (APIException e) {
						log.warn("Error deleting tribe", e);
						if (!error.equals("")) error += "<br/>";
						error += t + " " + notChanged;
					}
				}
			}
			
			view = getSuccessView();
			if (!success.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			if (!error.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		log.info("formBackingObject");

		//default empty Object
		List<Tribe> tribeList = new Vector<Tribe>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			TribeService ts = ((TribeService)Context.getService(TribeService.class));
	    	tribeList = ts.getTribes();
		}
    	
        return tribeList;
    }
    
}