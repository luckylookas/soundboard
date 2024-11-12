import React from 'react';
import { OutputState} from "./api";

interface Props {
    outputs: OutputState[]
    onSelect: (output: OutputState) => void
    selected?: OutputState
}

function OutputsOverviewComponent({outputs, onSelect, selected}: Props) {
    return <div className='flex-row'>
        {outputs
            .map(output => <div
                onClick={() => onSelect(output)}
                className={`flex-col ${selected === output ? 'bg-amber-500' : 'bg-amber-100 hover:bg-amber-300 cursor-pointer select-none'}`}
                                key={output.label ? output.label : output.name}>
                <div className={`flex w-full justify-center align-middle text-2xl py-1 cursor-pointer select-none
                ${selected === output ? 'font-bold' : ''}
                
                `}>{output.label ? output.label : output.name}</div>
                <div className={`cursor-pointer select-none flex w-full justify-center align-middle text-lg ${output.state === 'PLAYING' ? 'bg-lime-400' : 'bg-slate-500 opacity-75'}`}>{output.state} {output.currentlyPlaying ? `(${output.currentlyPlaying})` : '' }</div>
            </div>)
        }
        </div>

}

export default OutputsOverviewComponent;
