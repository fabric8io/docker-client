#!/usr/bin/groovy

def updateDependencies(source){

  def properties = []
  properties << ['<mockwebserver.version>','io/fabric8/mockwebserver']

  updatePropertyVersion{
    updates = properties
    repository = source
    project = 'fabric8io/docker-client'
  }
}

def stage(){
  return stageProject{
    project = 'fabric8io/docker-client'
    useGitTagForNextVersion = true
  }
}

def release(project){
  releaseProject{
    stagedProject = project
    useGitTagForNextVersion = true
    helmPush = false
    groupId = 'io.fabric8'
    githubOrganisation = 'fabric8io'
    artifactIdToWatchInCentral = 'docker-client'
    artifactExtensionToWatchInCentral = 'jar'
  }
}

def mergePullRequest(prId){
  mergeAndWaitForPullRequest{
    project = 'fabric8io/docker-client'
    pullRequestId = prId
  }

}
return this;
