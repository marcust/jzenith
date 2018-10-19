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

import org.junit.jupiter.api.Test;
import org.jzenith.core.JZenithException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LifeCycleObjectRepositoryTest {

    @Test
    public void testCloser() throws Exception {
        final LifeCycleObjectRepository repository = new LifeCycleObjectRepository();
        final Closer closer = mock(Closer.class);

        repository.register(closer);
        repository.closeAll();

        verify(closer, times(1)).close();
    }

    @Test
    public void testCloserException() {
        assertThrows(JZenithException.class, () -> {

            final LifeCycleObjectRepository repository = new LifeCycleObjectRepository();
            final Closer closer = mock(Closer.class);
            doThrow(new IOException("Foo")).when(closer).close();

            repository.register(closer);
            repository.closeAll();
        });
    }
}
