import React, {useEffect, useState} from 'react';
import {Library, SoundFile} from "./api";

function LibraryComponent() {
    const [collections, setCollections] = useState<string[]>([])
    const [selectedCollection, setSelectedCollection] = useState<string>('')
    const [files, setFiles] = useState<SoundFile[]>([])

    useEffect(() => {
        if (!selectedCollection) {
            setFiles([])
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
                        className={`${selectedCollection === it ? 'bg-amber-100 px-2' : 'hover:bg-amber-100 hover:px-2' } flex-1 bg-amber-200 hover:px-2 cursor-pointer`}
                        onClick={() => setSelectedCollection(it)}
                    >{it}</li>)
                }
            </ul>
        </div>
        <div className='flex w-1/2'>
            <ul className='flex-col flex-grow'>
                {files.map(file => <li
                    className={"flex-1 bg-amber-200 hover:px-2 cursor-pointer hover:bg-amber-100"}>{file.name}</li>)}
            </ul>
        </div>
    </div>

}

export default LibraryComponent;