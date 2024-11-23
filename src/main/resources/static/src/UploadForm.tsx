import React, {useState} from 'react';
import {Library} from "./api";
import {Button, Slider, Toggle} from "./components/Button";

function UploadForm() {
    const [loop, setLoop] = useState(false)
    const [volume, setVolume] = useState(100)
    const [file, setFile] = useState<File | null | undefined>(null)
    return <div className={`flex flex-col w-full h-full rounded justify-between bg-emerald-50 p-2 m-2 shadow-2xl shadow-emerald-950`}>

        <div className={'flex flex-col'}>
            <Toggle onClick={() => setLoop(old => !old)} color={"emerald"} checked={loop}>{loop ? 'loop on' : 'loop off'}</Toggle>
            <Slider color={'emerald'} value={volume} onChange={setVolume}><span className={`text-neutral-800`}>{`volume ${volume}%`}</span></Slider>

            <label htmlFor={"file"} className={`bg- p-2 m-2 flex drop-shadow-lg rounded bg-emerald-300 hover:bg-emerald-200 text-neutral-800 justify-center items-center`}> {file ? `change file (${file.name})` : 'select file'}</label>
            <input
                className={`hidden`}
                type='file'
                accept='.mp3'
                name='file'
                id={"file"}
                onChange={e => {
                    setFile(e.target.files?.item(0))
                }
                }/>
        </div>

        <div className={'flex flex-col'}>
            <Button disabled={!file} onClick={() => {
                Library.upload(file!, loop, volume).catch()
            }} color={"emerald"} >
                upload
            </Button>
        </div>

    </div>
}

export default UploadForm;
