ButterKnifeZelezny
===================

simple plug-in for IntelliJ IDEA and Android Studio that allows one-click creation of ButterKnife view injections

INSTALLATION
============

- download android-butterknife-zelezny_java6.jar or android-butterknife-zelezny_java6.jar file from repository's root
- IDEA/Android Studio → Preferences → Plugins → Install plugin from disk → select downloaded .jar file

USAGE
=====

- right click on usage of desired layout file (e.g. setContentView(R.layout.main))
- select 'Generate'
- select 'Generate Butterknife Injections'

You need to include Butterknife library [https://github.com/JakeWharton/butterknife] into your project in order to make this work.

OTHER'S WORK
============

- IDEA code generator by Anatoly Korniltsev [https://github.com/kurganec/intellij-android-codegenerator/]

LICENSE
=======

<pre>Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
you may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.</pre>
