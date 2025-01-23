import React, {useContext, useEffect, useState} from 'react';
import {Box, Button, HtmlTextInput} from "./components/Button";
import {AdventureContext} from "./Context";
import {Adventure, AdventureApi, DevicesApi, FilesApi, GameApi, Output, SoundDevice, SoundFile} from "./api";
import {NavLink, useNavigate, useParams} from "react-router";


export function AdventureList() {
    const [list, setList] = useState<Adventure[]>([])
    const [name, setName] = useState('')
    let navigate = useNavigate();

    useEffect(() => {
        AdventureApi.find("").then(it => setList(it))
    }, []);

    return <Box onBack={() => {
    }} title={"Adventures"}>
        <div className={`flex flex-col gap-2`}>
        {list.map(it => <NavLink className={`flex font-bold text-xl items-center justify-center p-1 border-b-2 border-neutral-800 hover:bg-emerald-200`} to={`/${it.id}`}>{it.name}</NavLink>)}
        <div className={`mt-2 p-1 border-b-2 border-neutral-800 flex flex-row gap-2`}>
            <HtmlTextInput onChange={(e) => setName(e ?? '')} id={'adventureName'} value={name} inputProps={{}} />
            <Button disabled={!name} onClick={() => AdventureApi.create(name).then((r) => navigate(`/${r.id}`))} text={'create'} />

        </div>
        </div>
    </Box>
}

function AdventureForm() {
    const [context, setContext] = useContext(AdventureContext)
    let {adventureId, sceneId} = useParams();
    let navigate = useNavigate();

    useEffect(() => {
        setContext(prev => ({
            ...prev, refresh: () => AdventureApi.get(Number.parseInt(adventureId!, 10))
                .then(it => setContext(prev => ({
                    ...prev,
                    adventure: it!
                })))
        }))
    }, [adventureId]);

    useEffect(() => {
        if (context?.refresh) {
            context.refresh()
        }
    }, [context?.refresh]);

    useEffect(() => {
        setContext(prev => ({...prev, selectedSceneId: sceneId ? Number.parseInt(sceneId, 10) : undefined}))
    }, [sceneId]);

    if (context?.adventure) {
        return <Box onBack={() => {
            navigate("/")
        }} title={context!.adventure!.name}>
            <AdventureComponent/>
        </Box>
    } else {
        return null
    }


}

function AdventureComponent() {
    const [context, setContext] = useContext(AdventureContext)
    const [draggedFile, setDraggedFile] = useState<SoundFile>()
    const [draggedDevice, setDraggedDevice] = useState<SoundDevice>()
    const [dragOver, setDragOver] = useState<Output>()

    const [newSceneName, setNewSceneName] = useState('')
    const [newOutputName, setNewOutputName] = useState('')

    const navigate = useNavigate()

    return <div className={`flex flex-row w-full h-full`}>
        <div className={'flex basis-1/4 flex-col border-r-2 border-neutral-800'}>
            {context?.adventure?.scenes?.map(s =>
               <div className={`border-b-2 border-neutral-800 flex flex-row`}>
                   <NavLink to={`/${context?.adventure?.id}/${s.id}`} className={`
                ${context?.selectedSceneId && context.selectedSceneId === s.id ? `bg-rose-200` : `cursor-pointer hover:bg-emerald-200`}
                w-full p-2`}>{s.name}</NavLink>
                   <div onClick={() => AdventureApi.removeScene(context?.adventure?.id!, s.id!).then(() => context?.refresh!())} className={`border-neutral-800 border-l-2 cursor-pointer bg-rose-800 p-2 text-xl font-bold hover:bg-rose-400`}>X</div>
               </div> )}

            <div className={`border-b-2 border-neutral-800`}>
                <HtmlTextInput onChange={(e) => setNewSceneName(e ?? '')} id={'scene'} value={newSceneName} inputProps={{}} />
                <Button disabled={!newSceneName} onClick={() => AdventureApi.createScene(context?.adventure?.id!, {
                    name: newSceneName,
                    outputs: []
                } )
                    .then((r) => navigate(`/${context?.adventure?.id!}/${r.scenes.find(it => it.name === newSceneName)?.id ?? '1'}`))
                    .then(() => context?.refresh!())
                    .then(() => setNewSceneName(''))} text={'create'} />

            </div>
        </div>
        <div className={`flex basis-3/4 flex-col p-1`}>
            {[context?.adventure?.scenes?.find(it => it.id === context?.selectedSceneId)].filter(it => !!it).map(it =>
                <div>

                    <div className={`text-xl font-bold`}>{it!.name}</div>
                    <div>
                        {it!.outputs?.map(o => <div
                            className={`w-full my-2 border-b-2 border-neutral-200 drop-shadow-lg bg-neutral-50 flex flex-col`}
                            onDrop={(e) => {
                                if (e.dataTransfer.getData('device')) {
                                    DevicesApi.assign(Number.parseInt(e.dataTransfer.getData('device'), 10), o.id!)
                                        .then(() => context?.refresh!())
                                }
                                if (e.dataTransfer.getData('file')) {
                                    AdventureApi.addFile(context?.adventure!.id!, it!.id!, o.id!, Number.parseInt(e.dataTransfer.getData('file'), 10), false)
                                        .then(() => context?.refresh!())
                                }
                                setDragOver(undefined)
                            }}
                            onDragOver={(e) => {
                                e.preventDefault()
                            }}
                            onDragEnter={() => setDragOver(o)}
                            onDragLeave={() => setDragOver(undefined)}>
                            <div className={`flex flex-row w-full justify-between pointer-events-none bg-neutral-100`}>
                                <div
                                    className={`flex justify-center ${draggedDevice ? 'pointer-events-none' : 'pointer-events-auto'}
                                     p-1 
                                     border-l-2 border-r-2 border-rose-200
                                     hover:border-rose-500
                                     basis-1/12
                                     hover:bg-rose-200 cursor-pointer`}
                                    onClick={() => GameApi.stop(o.id!).then(() => context?.refresh!())}>stop
                                </div>

                                <div className={`p-1 pointer-events-none text-xl`}>{o.name}</div>


                                <div
                                    className={`flex flex-row justify-end`}>
                                    {o.devices?.map(d =>
                                        <div
                                            onClick={() => DevicesApi.unassign(d.id, o.id!).then(() => context?.refresh!())}
                                            className={`border-l-2 border-r-2 bg-neutral-100 border-neutral-200 p-1 hover:line-through hover:bg-rose-200 cursor-pointer ${draggedDevice || draggedFile ? 'pointer-events-none' : 'pointer-events-auto'} hover:line-through`}
                                        >{d.name}</div>)}
                                    {draggedDevice && dragOver === o && <div
                                        className={`p-1 pointer-events-none`}>{draggedDevice.name}</div>}

                                    <div
                                        className={`cursor-pointer  p-1 px-2 border-r-2 border-l-2 border-neutral-200 hover:bg-rose-200 hover:border-rose-500 ${draggedDevice || draggedFile ? 'pointer-events-none' : 'pointer-events-auto'}`}
                                        onClick={() => AdventureApi.removeOutput(context?.adventure!.id!, it!.id!, o.id!).then(() => context?.refresh!())}>X
                                    </div>
                                </div>

                            </div>

                            <div className={`${draggedFile || draggedDevice ? `pointer-events-none` : ''}`}>
                                {draggedFile && dragOver === o && <div
                                    className={`pointer-events-none`}>{draggedFile.name}</div>}

                                {o.files?.map(f =>
                                    <div
                                        className={`${draggedFile || draggedDevice ? `pointer-events-none` : ''} justify-between flex-row flex border-b-2  mt-1 select-none`}>


                                        <div
                                            onClick={() => GameApi.play(o.id!, f.id).then(() => context?.refresh!())}
                                            className={`basis-1/12 border-r-2 border-l-2 px-2 hover:bg-emerald-200 hover:border-emerald-500 select-none p-1 cursor-pointer`}>play
                                        </div>


                                        <div
                                            className={`flex justify-center items-center align-middle`}>{f.name} {f.loop ? '(looping)' : ''} {o.playOnStart?.id == f.id ? '(default)' : ''}</div>


                                        <div className={`flex flex-row justify-end`}>

                                            {o.playOnStart?.id === f.id ?
                                                <div
                                                    className={`border-neutral-200 border-l-2 select-none bg-rose-200 px-2 hover:bg-emerald-200 p-1 cursor-pointer`}
                                                    onClick={() => {
                                                        AdventureApi.addFile(context?.adventure!.id!, it!.id!, o.id!, f.id, false)
                                                            .then(() => context?.refresh!())
                                                    }}>unset default
                                                </div> :
                                                <div
                                                    className={`select-none border-r-2 border-l-2 border-neutral-200 px-2 hover:bg-emerald-200 hover:border-emerald-500 p-1 cursor-pointer`}
                                                    onClick={() => {
                                                        AdventureApi.addFile(context?.adventure!.id!, it!.id!, o.id!, f.id, true)
                                                            .then(() => context?.refresh!())
                                                    }}>make default
                                                </div>
                                            }


                                            <div
                                                className={`cursor-pointer  p-1 px-2 border-r-2 border-l-2 border-neutral-200 hover:bg-rose-200 hover:border-rose-500`}
                                                onClick={() => AdventureApi.removeFile(context?.adventure!.id!, it!.id!, o.id!, f.id).then(() => context?.refresh!())}>X
                                            </div>
                                        </div>
                                    </div>)}
                            </div>
                        </div>)}
                        <div className={`border-b-2 border-neutral-800`}>
                            <HtmlTextInput onChange={(e) => setNewOutputName(e ?? '')} id={'output'} value={newOutputName}
                                           inputProps={{}}/>
                            <Button disabled={!newOutputName}
                                    onClick={() => AdventureApi.addOutput(context?.adventure?.id!, it!.id!, {
                                        devices: [],
                                        files: [],
                                        name: newOutputName
                                    })
                                        .then(() => context?.refresh!())
                                        .then(() => setNewSceneName(''))} text={'create'}/>

                        </div>
                    </div>
                </div>)}
        </div>
        <div className={'flex basis-1/4 flex-col p-1 border-l-2 border-neutral-800'}>

            <div className={`flex flex-col w-full h-1/4 overflow-y-scroll`}>
                <div className={`cursor-pointer bg-rose-400 hover:bg-rose-200 w-full p-2 `}
                     onClick={() => DevicesApi.rescan().then(DevicesApi.list).then(it => setContext(prev => ({
                         ...prev,
                         devices: it
                     })))}>rescan
                </div>
                {context?.devices?.map(d => <div draggable={true} onDragStart={(e) => {
                    e.dataTransfer.setData('device', `${d.id}`);
                    setDraggedDevice(d)
                }} onDragEnd={() => {setDraggedDevice(undefined)}} className={`dra cursor-grab bg-emerald-200 hover:bg-emerald-200 w-full p-2 `}>{d.name} ({d.currentlyPlaying?.name ?? 'idle'})</div>)}
            </div>
            <div className={`flex flex-col w-full h-full border-neutral-200 border-t-2 overflow-y-scroll`}>
                <div className={`cursor-pointer bg-rose-400 hover:bg-rose-200 w-full p-2`}>rescan (?)</div>
                {context?.files?.map(f => <div draggable={true} onDragStart={(e) => {
                    e.dataTransfer.setData('file', `${f.id}`);
                    setDraggedFile(f)
                }} onDragEnd={() => {setDraggedFile(undefined)}} className={`dra cursor-grab bg-emerald-200 hover:bg-emerald-200 w-full p-2`}>{f.name}</div>)}

            </div>
        </div>
    </div>
}


export default AdventureForm;
