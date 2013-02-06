<!--

    Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->

this folder is used by nar plugin during packaging or nar artifacts;

by convention, layout is:

src/main/resources/aol/${aol}/lib/lib-name.lib-extension

where ${aol} is current arch-os-linker signature;

during IDE testing, these resources can be loaded from path:
/target/test-classes/aol/${aol}/lib/lib-name.lib-extension

during NAR packaging, these resources will be included on nar/jar path: 
/lib/${aol}/${type}/lib-name.lib-extension
