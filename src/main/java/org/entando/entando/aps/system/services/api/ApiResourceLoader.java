/*
*
* Copyright 2012 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
* This file is part of Entando software.
* Entando is a free software; 
* you can redistribute it and/or modify it
* under the terms of the GNU General Public License (GPL) as published by the Free Software Foundation; version 2.
* 
* See the file License for the specific language governing permissions   
* and limitations under the License
* 
* 
* 
* Copyright 2012 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
*/
package org.entando.entando.aps.system.services.api;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.entando.entando.aps.system.services.api.model.ApiResource;
import org.springframework.core.io.Resource;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.util.ApsWebApplicationUtils;
import com.agiletec.aps.util.FileTextReader;

/**
 * Shortcut Loader Class.
 * @author E.Santoboni
 */
public class ApiResourceLoader {
    
    protected ApiResourceLoader(String locationPatterns, ServletContext servletContext) throws ApsSystemException {
        try {
            StringTokenizer tokenizer = new StringTokenizer(locationPatterns, ",");
            while (tokenizer.hasMoreTokens()) {
                String locationPattern = tokenizer.nextToken().trim();
                this.loadApiResources(locationPattern, servletContext);
            }
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "ApiMethodLoader", "Error loading Api Method definitions");
            throw new ApsSystemException("Error loading Api Method definitions", t);
        }
    }
    
    private void loadApiResources(String locationPattern, ServletContext servletContext) throws Exception {
        Logger log = ApsSystemUtils.getLogger();
        Resource[] resources = ApsWebApplicationUtils.getResources(locationPattern, servletContext);
        ApiResourcesDefDOM dom = null;
        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            InputStream is = null;
            String path = resource.getURI().getPath();
            try {
                is = resource.getInputStream();
                String xml = FileTextReader.getText(is);
                dom = new ApiResourcesDefDOM(xml, path);
                Map<String, ApiResource> extractedResources = dom.getResources();
                if (null != extractedResources) {
                    Iterator<ApiResource> extractedResourcesIter = extractedResources.values().iterator();
                    while (extractedResourcesIter.hasNext()) {
                        ApiResource apiResource = extractedResourcesIter.next();
                        if (null != this.getResources().get(apiResource.getResourceName())) {
                            String alertMessage = "ALERT: Into definition file '" + path + "' "
                                    + "there is an API resource '" + apiResource.getResourceName() 
                                    + "' and there is just one already present - This definition will be ignored!!!";
                            ApsSystemUtils.getLogger().severe(alertMessage);
                        } else {
                            this.getResources().put(apiResource.getResourceName(), apiResource);
                        }
                    }
                }
                log.info("Loaded Api Resources definition by file " + path);
            } catch (Throwable t) {
                ApsSystemUtils.logThrowable(t, this, "loadApiResources", 
                        "Error loading Api Resources definition by location Pattern '" + path + "'");
            } finally {
                if (null != is) {
                    is.close();
                }
            }
        }
    }
    
    public Map<String, ApiResource> getResources() {
        return this._resources;
    }
    
    private Map<String, ApiResource> _resources = new HashMap<String, ApiResource>();
    
}