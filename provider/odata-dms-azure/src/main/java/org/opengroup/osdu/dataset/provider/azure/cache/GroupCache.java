/*
 * Copyright 2021  Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.opengroup.osdu.dataset.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.springframework.stereotype.Component;

// Todo: Use Redis / VmCache
@Component
public class GroupCache implements ICache<String, Groups> {
    @Override
    public void put(String s, Groups o) {

    }

    @Override
    public Groups get(String s) {
        return null;
    }

    @Override
    public void delete(String s) {

    }

    @Override
    public void clearAll() {

    }
}
