pipelineJob('StudentProj-CI') {
    parameters {
        stringParam('RELEASE_VERSION', 'NA', 'Enter Release Version')
    }
    definition {
        cps {
            script(readFileFromWorkspace('pipelines/StudentProjCI.groovy'))
            sandbox()
        }
    }
}
