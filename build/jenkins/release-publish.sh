#!/bin/bash

echo "##################################"

echo "WORKSPACE=$WORKSPACE"

echo "### push changes"
git push

echo "### push tags"
git tag | grep barchart-udt | xargs git push origin tag

echo "##################################"
