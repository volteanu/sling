/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.commons.auth.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>AuthenticationInfo</code> conveys any authentication credentials
 * and/or details extracted by the
 * {@link AuthenticationHandler#extractCredentials(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
 * method from the request.
 * <p>
 * {@link AuthenticationHandler} implementations must return instances of this
 * class which may be constructed through any of the provided public
 * constructors.
 * <p>
 * Internally all values are stored in the map where some property names have
 * special semantics and the data type of the properties are ensured by the
 * {@link #put(String, Object)} method implementation.
 */
@SuppressWarnings("serial")
public class AuthenticationInfo extends HashMap<String, Object> {

    /**
     * A special instance of this class which may be returned from the
     * {@link AuthenticationHandler#extractCredentials(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * method to inform the caller, that a response has been sent to the client
     * to request for credentials.
     * <p>
     * If this value is returned, the request should be considered finished and
     * no further actions should be taken on this request.
     */
    public static final AuthenticationInfo DOING_AUTH = new ReadOnlyAuthenticationInfo(
        "DOING_AUTH");

    /**
     * A special instance of this class which may be returned from the
     * {@link AuthenticationHandler#extractCredentials(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * method to inform the caller that credential extraction failed for some
     * reason.
     * <p>
     * If this value is returned, the handler signals that credentials would be
     * present in the request but the credentials are not valid for use (for
     * example OpenID identify failed to validate or some authentication cookie
     * expired).
     */
    public static final AuthenticationInfo FAIL_AUTH = new ReadOnlyAuthenticationInfo(
        "FAIL_AUTH");

    /**
     * The name of the special property providing the authentication type
     * provided by the {@link AuthenticationHandler}. This value must be
     * supplied to one of the constructors and is ultimately used as the value
     * of the <code>HttpServletRequest.getAuthType</code> method.
     * <p>
     * This property is always present (and cannot be removed) in this map and
     * is of <code>String</code> type.
     */
    public static final String AUTH_TYPE = "sling.authType";

    /**
     * The name of the property providing the name of the user on whose behalf
     * the request is being handled. This property is set by the
     * {@link #AuthenticationInfo(String, String, char[], String)} constructor
     * and may be <code>null</code> if this instance is created by either the
     * {@link #AuthenticationInfo(String, String)} or
     * {@link #AuthenticationInfo(String, String, char[])} constructors.
     * <p>
     * The type of this property, if present, is <code>String</code>.
     */
    public static final String USER = "user.name";

    /**
     * The name of the property providing the password of the user on whose
     * behalf the request is being handled. This property is set by the
     * {@link #AuthenticationInfo(String, String, char[], String)} constructor
     * and may be <code>null</code> if this instance is created by either the
     * {@link #AuthenticationInfo(String, String)} or
     * {@link #AuthenticationInfo(String, String, char[])} constructors.
     * <p>
     * The type of this property, if present, is <code>char[]</code>.
     */
    public static final String PASSWORD = "user.password";

    /**
     * The name of the property providing the JCR credentials. These credentials
     * are preset to the credentials given to the
     * {@link #AuthenticationInfo(String, String)} or
     * {@link #AuthenticationInfo(String, String, char[])} constructors. the
     * {@link #AuthenticationInfo(String, String, char[], String)} constructor
     * is used the credentials property is set to a JCR
     * <code>SimpleCredentials</code> instance containing the user id and
     * password passed to the constructor.
     */
    public static final String CREDENTIALS = "user.jcr.credentials";

    /**
     * The name of the property providing the name of the JCR workspace to which
     * the request should be connected. This property may be set by any of the
     * constructors. If this property is not set, the user will be connected to
     * a default workspace as defined by the JCR repository to which the request
     * is connected.
     * <p>
     * The type of this property, if present, is <code>String</code>.
     */
    public static final String WORKSPACE = "user.jcr.workspace";

    /**
     * Creates an instance of this class with just the authentication type. To
     * effectively use this instance the user Id with optional password and/or
     * the credentials should be set.
     *
     * @param authType The authentication type, must not be <code>null</code>.
     */
    public AuthenticationInfo(final String authType) {
        this(authType, null, null, null);
    }

    /**
     * Creates an instance of this class authenticating with the given type and
     * userid.
     *
     * @param authType The authentication type, must not be <code>null</code>.
     * @param userId The name of the user to authenticate as. This may be
     *            <code>null</code> for the constructor and later be set.
     * @throws NullPointerException if <code>authType</code> is
     *             <code>null</code>.
     */
    public AuthenticationInfo(final String authType, final String userId) {
        this(authType, userId, null, null);
    }

    /**
     * Creates an instance of this class authenticating with the given type and
     * userid/password connecting to the default workspace as if the
     * {@link #AuthenticationInfo(String, String, char[], String)} method would
     * be called with a <code>null</code> workspace name.
     *
     * @param authType The authentication type, must not be <code>null</code>.
     * @param userId The name of the user to authenticate as. This may be
     *            <code>null</code> for the constructor and later be set.
     * @param password The password to authenticate with or <code>null</code> if
     *            no password can be supplied.
     * @throws NullPointerException if <code>authType</code> is
     *             <code>null</code>.
     */
    public AuthenticationInfo(final String authType, final String userId,
            final char[] password) {
        this(authType, userId, password, null);
    }

    /**
     * Creates an instance of this class authenticating with the given type and
     * userid/password.
     *
     * @param authType The authentication type, must not be <code>null</code>.
     * @param userId The name of the user to authenticate as. This may be
     *            <code>null</code> for the constructor and later be set.
     * @param password The password to authenticate with or <code>null</code> if
     *            no password can be supplied.
     * @param workspaceName The name of the workspace to connect to, may be
     *            <code>null</code> to connect to the default workspace.
     * @throws NullPointerException if <code>authType</code> is
     *             <code>null</code>
     */
    public AuthenticationInfo(final String authType, final String userId,
            final char[] password, final String workspaceName) {
        if (authType == null) {
            throw new NullPointerException("authType");
        }

        super.put(AUTH_TYPE, authType);
        putIfNotNull(USER, userId);
        putIfNotNull(PASSWORD, password);
        putIfNotNull(WORKSPACE, workspaceName);
    }

    /**
     * @param authType The authentication type to set. If this is
     *            <code>null</code> the current authentication type is not
     *            replaced.
     */
    public final void setAuthType(String authType) {
        putIfNotNull(AUTH_TYPE, authType);
    }

    /**
     * Returns the authentication type stored as the {@link #AUTH_TYPE} property
     * in this map. This value is expected to never be <code>null</code>.
     * <p>
     * If authentication is taking place through one of the standard ways, such
     * as Basic or Digest, the return value is one of the predefined constants
     * of the <code>HttpServletRequest</code> interface. Otherwise the value may
     * be specific to the {@link AuthenticationHandler} implementation.
     */
    public final String getAuthType() {
        return (String) get(AUTH_TYPE);
    }

    /**
     * @param user The name of the user to authenticate as. If this is
     *            <code>null</code> the current user name is not replaced.
     */
    public final void setUser(String user) {
        putIfNotNull(USER, user);
    }

    /**
     * Returns the user name stored as the {@link #USER} property or
     * <code>null</code> if the user is not set in this map.
     */
    public final String getUser() {
        return (String) get(USER);
    }

    /**
     * @param password The password to authenticate with. If this is
     *            <code>null</code> the current password is not replaced.
     */
    public final void setPassword(char[] password) {
        putIfNotNull(PASSWORD, password);
    }

    /**
     * Returns the password stored as the {@link #PASSWORD} property or
     * <code>null</code> if the password is not set in this map.
     */
    public final char[] getPassword() {
        return (char[]) get(PASSWORD);
    }

    /**
     * Sets or resets a property with the given <code>key</code> to a new
     * <code>value</code>. Some keys have special meanings and their values are
     * required to have predefined as listed in the following table:
     * <table>
     * <tr>
     * <td>{@link #AUTH_TYPE}</td>
     * <td><code>String</code></td>
     * </tr>
     * <tr>
     * <td>{@link #USER}</td>
     * <td><code>String</code></td>
     * </tr>
     * <tr>
     * <td>{@link #PASSWORD}</td>
     * <td><code>char[]</code></td>
     * </tr>
     * <tr>
     * <td>{@link #CREDENTIALS}</td>
     * <td><code>javax.jcr.Credentials</code></td>
     * </tr>
     * <tr>
     * <td>{@link #WORKSPACE}</td>
     * <td><code>String</code></td>
     * </tr>
     * </table>
     * <p>
     * If the value for the special key does not match the required type an
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param key The name of the property to set
     * @param value The value of the property which must be of a special type if
     *            the <code>key</code> designates one of the predefined
     *            properties.
     * @return The value previously set for the given <code>key</code>.
     * @throws IllegalArgumentException if <code>key</code> designates one of
     *             the special properties and the <code>value</code> does not
     *             have the correct type for the respective key.
     */
    @Override
    public Object put(final String key, final Object value) {

        if (AUTH_TYPE.equals(key) && !(value instanceof String)) {
            throw new IllegalArgumentException(AUTH_TYPE
                + " property must be a String");
        }

        if (USER.equals(key) && !(value instanceof String)) {
            throw new IllegalArgumentException(USER
                + " property must be a String");
        }

        if (PASSWORD.equals(key) && !(value instanceof char[])) {
            throw new IllegalArgumentException(PASSWORD
                + " property must be a char[]");
        }

        if (CREDENTIALS.equals(key) && !isCredentialsObject(value)) {
            throw new IllegalArgumentException(CREDENTIALS
                + " property must be a javax.jcr.Credentials instance");
        }

        if (WORKSPACE.equals(key) && !(value instanceof String)) {
            throw new IllegalArgumentException(WORKSPACE
                + " property must be a String");
        }

        return super.put(key, value);
    }

    /** We do this check in order to avoid an import to the javax.jcr.* package */
    private boolean isCredentialsObject(final Object value) {
        if ( value == null ) {
            return false;
        }
        Class<?> checkClass = value.getClass();
        do {
            final Class<?>[] interfaces = value.getClass().getInterfaces();
            for(final Class<?> current : interfaces) {
                if ( current.getName().equals("javax.jcr.Credentials") ) {
                    return true;
                }
            }
            checkClass = checkClass.getSuperclass();
        } while ( checkClass != null );
        return false;
    }

    /**
     * Removes the entry with the given <code>key</code> and returns its former
     * value (if existing). If the <code>key</code> is {@link #AUTH_TYPE} the
     * value is not actually removed and <code>null</code> is always returned.
     *
     * @param key Removes the value associated with this key.
     * @return The former value associated with the key.
     */
    @Override
    public Object remove(Object key) {

        // don't remove the auth type from the map
        if (AUTH_TYPE.equals(key)) {
            return null;
        }

        return super.remove(key);
    }

    /**
     * Clears all properties from the map with the exception of the
     * {@link #AUTH_TYPE} property.
     */
    @Override
    public void clear() {
        final String authType = getAuthType();
        super.clear();
        setAuthType(authType);
    }

    /**
     * Calls {@link #put(String, Object)} only if the <code>value</code> is not
     * <code>null</code>, otherwise does nothing.
     *
     * @param key The key of the property to set
     * @param value The value to set for the property. This may be
     *            <code>null</code> in which case this method does nothing.
     */
    private void putIfNotNull(final String key, final Object value) {
        if (value != null) {
            put(key, value);
        }
    }

    /**
     * The <code>ReadOnlyAuthenticationInfo</code> extends the
     * {@link AuthenticationInfo} class overwriting the
     * {@link #put(String, Object)} method such that no property is ever set.
     * This acts like kind of a read-only wrapper.
     */
    private static final class ReadOnlyAuthenticationInfo extends
            AuthenticationInfo {

        /**
         * Creates an instance of this read-only class with the given
         * authentication type.
         *
         * @param authType The authentication type to set
         */
        ReadOnlyAuthenticationInfo(final String authType) {
            super(authType);
        }

        /**
         * Does not put the property into the map
         */
        @Override
        public Object put(String key, Object value) {
            return null;
        }

        /**
         * Sets/adds non of the properties
         */
        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
        }

        /**
         * Does not clear the properties
         */
        @Override
        public void clear() {
        }

        /**
         * Removes nothing
         */
        @Override
        public Object remove(Object key) {
            return null;
        }
    }
}
