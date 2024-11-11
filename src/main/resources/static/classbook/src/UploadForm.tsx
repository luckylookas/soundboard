import React, {useState} from 'react';
import {Library} from "./api";

function UploadForm() {
    const [name, setName] = useState('')
    const [collection, setCollection] = useState('default')

    return <div>
        <input name='collection' value={collection} onChange={e => setCollection(e.target.value)}/>
        <input name='name' value={name} onChange={e => setName(e.target.value)}/>
        <input type='file'
               accept='.mp3'
               name='file'
               onChange={e => {
                   const n = name ?
                       (name.endsWith(".mp3") ? name : name + '.mp3')
                       :
                       e.target.files!.item(0)!.name
                   setName(n)
                   Library.upload(collection, e.target.files!.item(0)!, name ?
                       (name.endsWith(".mp3") ? name : name + '.mp3')
                       :
                       e.target.files!.item(0)!.name)
                       .then(() => {
                           setName('')
                       })
                       .catch()
               }
               }/>
    </div>
}

export default UploadForm;
