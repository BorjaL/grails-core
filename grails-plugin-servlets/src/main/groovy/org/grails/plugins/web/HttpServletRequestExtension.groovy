/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugins.web

import javax.servlet.http.HttpServletRequest

import org.grails.web.util.WebUtils

/**
 * 
 * @author Jeff Brown
 * @since 3.0
 * 
 */
class HttpServletRequestExtension {

    static String getForwardURI(HttpServletRequest request) {
        WebUtils.getForwardURI request
    }

    static getProperty(HttpServletRequest request, String name) {
        def mp = request.getClass().metaClass.getMetaProperty(name)
        mp ? mp.getProperty(request) : request.getAttribute(name)
    }

    static propertyMissing(HttpServletRequest request, String name) {
        getProperty request, name
    }

    static propertyMissing(HttpServletRequest request, String name, value) {
        def mp = request.class.metaClass.getMetaProperty(name)
        if (mp) {
            mp.setProperty request, value
        }
        else {
            request.setAttribute name, value
        }
    }

    static getAt(HttpServletRequest request, String name) {
        getProperty request, name
    }

    static each(HttpServletRequest request, Closure c) {
        for (name in request.attributeNames) {
            switch (c.parameterTypes.length) {
                case 0:
                    c.call()
                    break
                case 1:
                    c.call(key:name, value:request.getAttribute(name))
                    break
                default:
                    c.call(name, request.getAttribute(name))
            }
        }
    }

    static find(HttpServletRequest request, Closure c) {
        def result = [:]
        for (name in request.attributeNames) {
            boolean match = false
            switch (c.parameterTypes.length) {
                case 0:
                    match = c.call()
                    break
                case 1:
                    match = c.call(key:name, value:request.getAttribute(name))
                    break
                default:
                    match =  c.call(name, request.getAttribute(name))
            }
            if (match) {
                result[name] = request.getAttribute(name)
                break
            }
        }
        result
    }

    static findAll(HttpServletRequest request, Closure c) {
        def results = [:]
        for (name in request.attributeNames) {
            boolean match = false
            switch (c.parameterTypes.length) {
                case 0:
                    match = c.call()
                    break
                case 1:
                    match = c.call(key:name, value:request.getAttribute(name))
                    break
                default:
                    match =  c.call(name, request.getAttribute(name))
            }
            if (match) {
                results[name] = request.getAttribute(name)
            }
        }
        results
    }

    static boolean isXhr(HttpServletRequest instance) {
        // TODO grails.web.xhr.identifier support
        instance.getHeader('X-Requested-With') == "XMLHttpRequest"
    }

    static boolean isGet(HttpServletRequest request) {
        request.method == 'GET'
    }

    static boolean isPost(HttpServletRequest request) {
        request.method == 'POST'
    }
}
