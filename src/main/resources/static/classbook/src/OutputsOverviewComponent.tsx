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
                className={`flex-col cursor-pointer select-none border-l-2 border-r-2 border-blue-500
                ${selected === output ? 'bg-blue-100' 
                    : ' hover:bg-blue-100'}`}
                                key={output.label ? output.label : output.name}>
                <div className={`flex w-full justify-center align-middle text-2xl py-1 cursor-pointer select-none
                ${selected === output ? 'font-bold' : ''}
                
                `}>{output.label ? output.label : output.name}</div>
                <div className={`cursor-pointer select-none flex w-full justify-center align-middle text-lg ${output.state === 'PLAYING' ? 'bg-emerald-500' : 'bg-transparent'}`}>{output.state} {output.currentlyPlaying ? `(${output.currentlyPlaying})` : '' }</div>
            </div>)
        }

        </div>

}

export default OutputsOverviewComponent;
