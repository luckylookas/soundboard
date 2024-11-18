import React, {useEffect, useState} from 'react';
import {Library, SoundFile} from "./api";

interface Props {
    onSelect: (file?: SoundFile) => void
    selected?: SoundFile
}

function LibraryComponent({onSelect, selected}: Props) {
    const [collections, setCollections] = useState<string[]>([])
    const [selectedCollection, setSelectedCollection] = useState<string>('')
    const [files, setFiles] = useState<SoundFile[]>([])

    useEffect(() => {
        if (!selectedCollection) {
            setFiles([])
            onSelect(undefined)
            return
        }
        Library.getCollection(selectedCollection).then(c => setFiles(c))
    }, [selectedCollection]);

    useEffect(() => {
        Library.getCollections().then(it => setCollections(it))
    }, []);

    return<div className='flex flex-row flex-nowrap'>
        <div className='flex w-1/2'>
            <ul className='flex-col flex-grow'>
                {
                    collections.map(it => <li
                        className={`${selectedCollection === it ? 'px-2' : 'hover:bg-blue-100 hover:px-2' } flex-1 bg-amber-200 hover:px-2 cursor-pointer`}
                        onClick={() => setSelectedCollection(it)}
                    >{it}</li>)
                }
            </ul>
        </div>
        <div className='flex w-1/2'>
            <ul className='flex-col flex-grow'>
                {files.map(file => <li
                    onClick={e => onSelect(file)}
                    className={"flex-1 bg-amber-200 hover:px-2 cursor-pointer hover:bg-amber-100"}>{file.name}</li>)}
            </ul>
        </div>
    </div>

}

export default LibraryComponent;
