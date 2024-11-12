import React, {useEffect, useState} from 'react';
import {Output, OutputState} from "./api";

function OutputsComponent() {

    const [outputs, setOutputs] = useState<OutputState[]>([])
    const [selectedOutput, setSelectedOutput] = useState<OutputState>()
    const [label, setLabel] = useState<string>('')
    useEffect(() => {
        Output.list().then(it => setOutputs(it))
    }, []);

    useEffect(() => {
        setLabel(selectedOutput?.label ?? '')
    }, [selectedOutput]);

    return<div className='flex flex-row flex-nowrap'>
        <div className='flex'>
            <ul className='flex-col flex-grow'>
                {
                    outputs.map(it =>
                        selectedOutput === it ?
                            <span className={`${selectedOutput === it ? 'bg-amber-100 px-2' : 'hover:bg-amber-100 hover:px-2' } flex-1 bg-amber-200 hover:px-2 cursor-pointer`}>
                                <input value={label} placeholder={selectedOutput.name} onChange={e => setLabel(e.target.value)}/>
                                <button disabled={!label} onClick={() => {
                                Output.relabel(selectedOutput?.name, label).then( () => Output.list()).then(it => {
                                    setOutputs(it)
                                    setSelectedOutput(it.find(o => o.label === label))
                                })
                            }}>relabel</button> </span>
                            :
                        <li
                        className={`${selectedOutput === it ? 'bg-amber-100 px-2' : 'hover:bg-amber-100 hover:px-2' } flex-1 bg-amber-200 hover:px-2 cursor-pointer`}
                        onClick={() => setSelectedOutput(it)}
                    >{`${it.label ? it.label : it.name}`} </li>)
                }
            </ul>
        </div>
    </div>
}

export default OutputsComponent;
