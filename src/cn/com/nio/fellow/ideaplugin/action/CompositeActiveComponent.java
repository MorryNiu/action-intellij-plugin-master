/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.com.nio.fellow.ideaplugin.action;

import com.intellij.ui.ActiveComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * related to visualisation of the composite activities through using Swing
 */
class CompositeActiveComponent implements ActiveComponent {
    private final ActiveComponent[] myComponents;
    private final JPanel panel;

    public CompositeActiveComponent(@NotNull ActiveComponent... components) {
        myComponents = components;

        // create a panel with double-buffered and flow layout (by default)
        panel = new JPanel();
        // make the panel transparent
        panel.setOpaque(false);
        //remove border
        panel.setBorder(null);

        for (ActiveComponent component : components) {
            panel.add(component.getComponent());
        }
    }

    @Override
    public void setActive(boolean active) {
        for (ActiveComponent component : myComponents) {
            component.setActive(active);
        }
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }
}