import React, {useEffect, useState} from 'react';
import {Output, OutputState} from "./api";

function App() {

    const [outputs, setOutputs] = useState<OutputState[]>([])
    const [selectedOutput, setSelectedOutput] = useState<OutputState>()

    useEffect(() => {
        Output.list().then(it => setOutputs(it))
    }, []);

    return<div className='flex flex-row flex-nowrap'>
        <div className='flex'>
            <ul className='flex-col flex-grow'>
                {
                    outputs.map(it => <li
                        className={`${selectedOutput === it ? 'bg-amber-100 px-2' : 'hover:bg-amber-100 hover:px-2' } flex-1 bg-amber-200 hover:px-2 cursor-pointer`}
                        onClick={() => setSelectedOutput(it)}
                    >{ `${(it.label ? it.label : it.name)} ${!it.label ? `[unlabeled]`: ''}`}</li>)
                }
            </ul>
        </div>
    </div>

}

export default App;
