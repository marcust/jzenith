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

package org.jzenith.core.guice;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.jzenith.core.JZenithException;

import java.util.Set;

@Slf4j
public class LifeCycleObjectRepository {

    private final Set<Closer> closers = Sets.newConcurrentHashSet();

    void register(Closer closeable) {
        closers.add(closeable);
    }

    public void closeAll() {
        final Set<Closer> localClosers = Sets.newHashSet(this.closers);

        localClosers.forEach(c -> {
            try {
                c.close();
            } catch (Exception e) {
                Throwables.throwIfUnchecked(e);
                throw new JZenithException(e);
            }
        });
        this.closers.removeAll(localClosers);
    }
}
