<!--

    Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->

### Goals and options

clean validate --update-snapshots --show-version  --activate-profiles modules

clean deploy site --define skipTests --update-snapshots --activate-profiles modules,website,artifact-version,release-attach,package-bundle

### Release goals and options

-Dresume=false release:prepare release:perform --activate-profiles modules


### DryRun goals and options

-Dresume=false -DdryRun=true release:prepare   --activate-profiles modules
