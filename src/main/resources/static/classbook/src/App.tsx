import React, {useEffect, useState} from 'react';
import OutputsOverviewComponent from "./OutputsOverviewComponent";
import {Output, OutputState, SoundFile} from "./api";
import LibraryComponent from "./LibraryComponent";

function App() {

    const outputs = useState<OutputState[]>([])
    const [selectedOutput, setSelectedOutput] = useState<OutputState>()
    const [selectedSoundFile, setSelectedSoundFile] = useState<SoundFile>()

    useEffect(() => {
        Output.list().then(o => outputs[1](o))
    }, []);

    useEffect(() => {
        if (selectedOutput && selectedSoundFile) {
            Output.play(selectedOutput.label, selectedSoundFile, 70, false)
                .then(() => Output.list())
                .then(o => outputs[1](o))
                .then(() => {
                    setSelectedSoundFile(undefined)
                })
                .catch()
        }
    }, [selectedOutput, selectedSoundFile]);

    return <div id="body" className='w-full h-full p-0 m-0'>
        <div id="outputs_overview">
            <OutputsOverviewComponent selected={selectedOutput} onSelect={
                (o) => {
                    if (o !== selectedOutput)
                        setSelectedOutput(o)
                    else
                        Output.stop(o.label)
                            .then(() => Output.list())
                            .then(o => outputs[1](o))

                }
            }
                                      outputs={outputs[0]}></OutputsOverviewComponent>
        </div>
        <div id="outputs"></div>
        <div id="sounds">
            <LibraryComponent selected={selectedSoundFile} onSelect={setSelectedSoundFile}></LibraryComponent>
        </div>
    </div>
}

export default App;
