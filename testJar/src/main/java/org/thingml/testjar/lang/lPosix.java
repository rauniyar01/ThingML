/**
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
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingml.testjar.lang;

import java.io.File;
import org.thingml.testjar.Command;
import org.thingml.testjar.SimpleGeneratedTest;
import org.thingml.testjar.TestCase;

/**
 *
 * @author sintef
 */
public class lPosix extends TargetedLanguage {
    
    public lPosix() {
        compilerID = "posix";
    }

    @Override
    public Command compileTargeted(SimpleGeneratedTest t) {
        String[] execCmd = new String[1];
        execCmd[0] = "make";
        
        return new Command(execCmd, null, ".+", "Error at c compilation", new File(t.genCodeDir, "/_" + compilerID + "/" + t.name + "_Cfg"));
    }

    @Override
    public Command execTargeted(SimpleGeneratedTest t) {
        String prg = t.name + "_Cfg";
        
        //String[] execCmd = new String[1];
        //execCmd[0] = "./" + prg;
        String[] execCmd = new String[5];
        execCmd[0] = "timeout";
        execCmd[1] = "-s";
        execCmd[2] = "9";
        execCmd[3] = "30s";
        execCmd[4] = "./" + prg;
        
        return new Command(execCmd, ".+", null, "Error at c execution", new File(t.genCodeDir, "/_" + compilerID + "/" + t.name + "_Cfg"));
    }

    @Override
    public Command compileTargeted(File src) {
        String[] execCmd = new String[1];
        execCmd[0] = "make";
        
        return new Command(execCmd, null, ".+", "Error at c compilation", src);
    }
    
}
