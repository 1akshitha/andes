/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.andes.messageStore;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.wso2.andes.kernel.AndesException;
import org.wso2.andes.kernel.DurableStoreConnection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.SQLException;

// todo: need to implement with reading cassandra data source from master-datasources.xml
public class H2Connection implements DurableStoreConnection {

    private static final Logger logger = Logger.getLogger(H2Connection.class);
    private boolean isConnected;
    private String jndiLookupName;
    private DataSource datasource;


    public H2Connection(boolean isInMemoryMode) {
        if(isInMemoryMode) {
            jndiLookupName = JDBCConstants.H2_MEM_JNDI_LOOKUP_NAME;
        } else {
            jndiLookupName = JDBCConstants.H2_JNDI_LOOKUP_NAME;
        }
        isConnected = false;
    }

    // todo: remove argument
    @Override
    public void initialize(Configuration configuration) throws AndesException {
        Connection connection = null;
        isConnected = false;
        try {
            datasource = InitialContext.doLookup(jndiLookupName);
            connection = datasource.getConnection();
            isConnected = true; // if no errors
        } catch (SQLException e) {
            throw new AndesException("Connecting to H2 database failed!", e);
        } catch (NamingException e) {
            throw new AndesException("Couldn't look up jndi entry for " +
                    "\"" + jndiLookupName + "\"" + e);
        } finally {
            close(connection, "initialising database");
        }
    }

    public DataSource getDatasource() {
        return datasource;
    }

    @Override
    public void close() {
        isConnected = false;
    }

    @Override
    public boolean isLive() {
        return isConnected;
    }

    @Override
    public Object getConnection() {
        return this; //
    }



    void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }


    /**
     * Closes the provided connection. on failure log the error;
     *
     * @param connection Connection
     */
    private void close(Connection connection, String task) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Failed to close connection after " + task);
            }
        }
    }
}
