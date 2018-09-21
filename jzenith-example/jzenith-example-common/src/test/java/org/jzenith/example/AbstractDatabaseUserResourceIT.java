/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.example;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;

public abstract class AbstractDatabaseUserResourceIT extends AbstractUserResourceIT {

    @Before
    public void setup() throws Exception {
        super.setup();

        final IDatabaseConnection connection = getConnection();
        final IDataSet dataSet = new FlatXmlDataSetBuilder().build(AbstractDatabaseUserResourceIT.class.getResourceAsStream("/user.xml"));

        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);

    }

    protected abstract IDatabaseConnection getConnection() throws Exception;


}
