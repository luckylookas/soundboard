import React, {useContext, useEffect, useMemo, useState} from 'react';
import {Box, Button, HtmlTextInput, Toggle} from "./components/Button";
import {AdventureContext, Context} from "./Context";
import {Adventure, AdventureApi, Scene} from "./api";

function AdventureForm() {
    const ctx = useContext(AdventureContext)

    return <Box onBack={() => ctx[1](undefined)} title={ctx[0] ? ctx[0].name : "Adventures"}>
        {ctx[0] ? <AdventureComponent adventure={ctx[0]}/>
            :
            <AdventureSearch ctx={ctx}/>}
    </Box>
}

function AdventureComponent({adventure}: { adventure: Adventure }) {
    return <div>
        <SceneForm adventure={adventure} />
    </div>
}

function SceneForm({adventure}: { adventure: Adventure }) {
    const ctx = useContext(AdventureContext)

    const [selectedScene, setSelectedScene] = useState<Scene>()

    useEffect(() => {
        if (selectedScene?.id) {
            AdventureApi.assign(adventure.id, selectedScene).then(ctx[1])
        }
    }, [selectedScene]);

    useEffect(() => {
        if (selectedScene && !selectedScene.id) {
            setSelectedScene(adventure.scenes.find(it => it.name === selectedScene.name))
        }
    }, [adventure]);

    return <div className={`flex flex-col h-full`}>
        <div className={`basis-1/2 flex-none flex-grow-0 flex-shrink overflow-y-auto flex-col my-2`}>
            {adventure.scenes?.map(scene => <Toggle slim={true}
                checked={scene.id === selectedScene?.id}
                onClick={() => setSelectedScene(scene)}>{scene.name}</Toggle>)}
        </div>
        <Button onClick={() => setSelectedScene(undefined)} text={'new'} />

        <div className={`basis-1/2 flex-1 flex flex-col my-2`}>
            <HtmlTextInput value={selectedScene?.name ?? ''} id={'scene-name'} inputProps={{}} onChange={(v) => {
                if (v && v !== selectedScene?.name) {
                    setSelectedScene({
                        ...(selectedScene ?? {name: v, outputs: []}),
                        name: v
                    })
                }
            }}/>

            <div>
                {selectedScene?.outputs?.map(o => <div>
                    <div>{o.name}</div>
                </div>)}
            </div>

            {selectedScene && !selectedScene?.id && <Button onClick={() => {
                AdventureApi.assign(adventure.id, selectedScene)
                    .then(ctx[1])
            }} text={'create'}/>}
        </div>
    </div>
}


function AdventureSearch({ctx}: { ctx: Context }) {
    const [search, setSearch] = useState<string | undefined>('')
    const [list, setList] = useState<Adventure[]>([])
    const setAdventure = useMemo(() => ctx[1], [ctx])

    useEffect(() => {
        if (search) {
            AdventureApi.find(search).then(results => setList(results))
        } else {
            setList([])
        }
    }, [search]);
    return <div>
        <HtmlTextInput
            onSelect={(s) => setAdventure(s)}
            onChange={(s) => {
                setSearch(s)
            }} id={'search'} value={search} inputProps={{}} additionalValues={list}/>

        <Button onClick={() => {
            if (search) {
                AdventureApi.create(search).then((it) => setAdventure(it))
            }
        }} text={'create'}/>
    </div>
}


export default AdventureForm;
