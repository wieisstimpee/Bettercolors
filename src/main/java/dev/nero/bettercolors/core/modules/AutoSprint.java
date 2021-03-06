/*
 * Copyright 2018-2020 Bettercolors Contributors (https://github.com/N3ROO/Bettercolors)
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

package dev.nero.bettercolors.core.modules;

import dev.nero.bettercolors.engine.module.Module;
import dev.nero.bettercolors.engine.option.Option;
import dev.nero.bettercolors.core.wrapper.Wrapper;

import java.util.ArrayList;

public class AutoSprint extends Module {

    /**
     * @param toggleKey the toggle key (-1 -> none).
     * @param isActivated the initial state.
     */
    public AutoSprint(Integer toggleKey, Boolean isActivated) {
        super("Auto sprint", toggleKey, isActivated, "sprint_symbol.png", "[ASp]");
    }

    @Override
    public void onUpdate() {
        if(Wrapper.MC.player != null){
            if(Wrapper.MC.player.moveForward > 0 && !Wrapper.MC.player.isSprinting()){
                logInfo("Forcing player to run");
                Wrapper.MC.player.setSprinting(true);
            }
        }
    }

    public static ArrayList<Option> getDefaultOptions(){
        return new ArrayList<>();
    }
}
