#!/bin/bash

cd public/javascripts
npm install
npm run build
cd ../../
sbt run