/*
 * Copyright (C) 2016 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.fabric8.docker.examples;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {

    public static void main(String args[]) throws InterruptedException, IOException {

        if (args.length == 0) {
            System.err.println("Usage: Main <example name> <arguments>");
            System.err.println("Example: Main ImageBuilder http://localhost:2375 myImage /some/path docker.io myns");
            System.err.println("Optionally: ImagePushExample <docker host> <repo name> <path> <registry> <namespace>");
            return;
        }

        String exampleName = args[0];

        try {
            Class example = Class.forName(Main.class.getPackage().getName() + "." + exampleName);
            String[] exampleArgs = new String[args.length - 1];
            System.arraycopy(args, 1, exampleArgs, 0, args.length - 1);
            Method mainMethod = example.getDeclaredMethod("main",String[].class);
            mainMethod.invoke(example.newInstance(), (Object) exampleArgs);
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load example:"+exampleName);
        } catch (NoSuchMethodException e) {
            System.err.println("Could not find main method on:"+exampleName);
        } catch (IllegalAccessException e) {
            System.err.println("Could not access main on:"+exampleName);
        } catch (InstantiationException e) {
            System.err.println("Could not create instance of:"+exampleName);
        } catch (InvocationTargetException e) {
            System.err.println("Could not invoke main on:"+exampleName);
            e.printStackTrace(System.err);
        }
    }
}
