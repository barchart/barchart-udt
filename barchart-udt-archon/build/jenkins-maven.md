<!--

    Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->

final release stage jenkins project settings

### Goals and options

clean validate --update-snapshots --show-version  --activate-profiles modules

clean deploy site --define skipTests --update-snapshots --activate-profiles modules,website,artifact-version,release-attach,package-bundle

### Release goals and options

release:clean
release:prepare
release:perform

--define
localCheckout=true

--activate-profiles
modules,artifact-version,release-attach,package-bundle


### DryRun goals and options

release:clean
release:prepare
release:perform

--define
localCheckout=true

--activate-profiles
modules,artifact-version,release-attach,package-bundle

-DdryRun=true

--errors 
