/*
 * Copyright 2022 Johns Hopkins University
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
package org.eclipse.pass.object.serde;

import com.yahoo.elide.core.utils.coerce.converters.ElideTypeConverter;
import com.yahoo.elide.core.utils.coerce.converters.Serde;
import org.eclipse.pass.object.model.ContributorRole;

/**
 * Serializer/Deserializer class for ContributorRole
 *
 * @see ContributorRole
 */
@ElideTypeConverter(type = ContributorRole.class, name = "ContributorRole")
public class ContributorRoleSerde implements Serde<String, ContributorRole> {

    @Override
    public ContributorRole deserialize(String val) {
        return ContributorRole.of(val);
    }

    @Override
    public String serialize(ContributorRole val) {
        return val.getValue();
    }
}